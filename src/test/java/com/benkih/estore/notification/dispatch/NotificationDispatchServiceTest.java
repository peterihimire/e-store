package com.benkih.estore.notification.dispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benkih.estore.email.dto.EmailAttachment;
import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailService;
import com.benkih.estore.notification.entity.DeliveryAttempt;
import com.benkih.estore.notification.entity.Notification;
import com.benkih.estore.notification.enums.AttemptOutcome;
import com.benkih.estore.notification.enums.NotificationStatus;
import com.benkih.estore.notification.repository.DeliveryAttemptRepository;
import com.benkih.estore.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private NotificationRepository notificationRepository;
    @Mock private DeliveryAttemptRepository deliveryAttemptRepository;
    @Mock private EmailService emailService;

    private NotificationDispatchService service;

    @BeforeEach
    void setUp() {
        service =
                new NotificationDispatchServiceImpl(
                        notificationRepository,
                        deliveryAttemptRepository,
                        emailService,
                        objectMapper);

        ReflectionTestUtils.setField(service, "provider", "mailgun");
        ReflectionTestUtils.setField(service, "maxAttempts", 3);
        ReflectionTestUtils.setField(service, "retryDelayMs", 100L);
        ReflectionTestUtils.setField(service, "claimLeaseMs", 60_000L);

        lenient()
                .when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(
                        invocation -> {
                            Notification saved = invocation.getArgument(0);
                            saved.setId(42L);
                            return saved;
                        });
    }

    @Test
    void prepareSkipsAnEventThatWasAlreadySent() {
        Notification sent = new Notification();
        sent.setSlug("already-sent");
        sent.setEventKey("order-confirmation:ORD-1");
        sent.setStatus(NotificationStatus.SENT);
        sent.setAttemptCount(1);

        when(notificationRepository.findByEventKey("order-confirmation:ORD-1"))
                .thenReturn(Optional.of(sent));

        Long id =
                service.prepare(
                        "order-confirmation:ORD-1", "order.confirmation", "receipt", request());

        assertNull(id);
        verify(emailService, never()).send(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void prepareWritesOutboxRowWithSerializedRequest() throws Exception {
        when(notificationRepository.findByEventKey(any())).thenReturn(Optional.empty());

        EmailRequest request = request();
        request.setAttachments(
                List.of(
                        new EmailAttachment(
                                "receipt.txt",
                                "text/plain",
                                "pdf-bytes".getBytes(StandardCharsets.UTF_8))));

        Long id = service.prepare("welcome:USER-1", "user.welcome", "welcome", request);

        assertEquals(42L, id);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(NotificationStatus.PENDING, saved.getStatus());
        assertEquals("buyer@example.com", saved.getRecipient());
        assertEquals("Your receipt", saved.getSubject());
        assertNotNull(saved.getNextAttemptAt());
        assertTrue(saved.getRequestPayload().contains("buyer@example.com"));
        assertEquals(
                "receipt.txt",
                objectMapper
                        .readTree(saved.getRequestPayload())
                        .get("attachments")
                        .get(0)
                        .get("filename")
                        .asText());
    }

    @Test
    void sendUsesStoredRequestAndRecordsProviderMessageId() {
        when(notificationRepository.findById(7L)).thenReturn(Optional.of(pendingNotification()));
        when(emailService.send(any())).thenReturn("msg-1");

        service.send(7L);

        ArgumentCaptor<EmailRequest> requestCaptor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService, times(1)).send(requestCaptor.capture());
        assertEquals("buyer@example.com", requestCaptor.getValue().getTo());
        assertEquals("Your receipt", requestCaptor.getValue().getSubject());
        assertEquals(1, requestCaptor.getValue().getAttachments().size());

        ArgumentCaptor<Notification> notificationCaptor =
                ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification saved = notificationCaptor.getValue();
        assertEquals(NotificationStatus.SENT, saved.getStatus());
        assertEquals(1, saved.getAttemptCount());
        assertEquals("msg-1", saved.getProviderMessageId());
        assertNull(saved.getNextAttemptAt());
        assertNotNull(saved.getSentAt());

        ArgumentCaptor<DeliveryAttempt> attemptCaptor =
                ArgumentCaptor.forClass(DeliveryAttempt.class);
        verify(deliveryAttemptRepository).save(attemptCaptor.capture());
        assertEquals(AttemptOutcome.SUCCESS, attemptCaptor.getValue().getOutcome());
        assertEquals("msg-1", attemptCaptor.getValue().getProviderMessageId());
        assertEquals("mailgun", attemptCaptor.getValue().getProvider());
    }

    @Test
    void sendRecordsTemporaryFailureAndSchedulesRetry() {
        when(notificationRepository.findById(7L)).thenReturn(Optional.of(pendingNotification()));
        when(emailService.send(any())).thenThrow(new RuntimeException("timeout"));

        service.send(7L);

        ArgumentCaptor<Notification> notificationCaptor =
                ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification saved = notificationCaptor.getValue();
        assertEquals(NotificationStatus.FAILED, saved.getStatus());
        assertEquals(1, saved.getAttemptCount());
        assertEquals("timeout", saved.getFailureReason());
        assertNotNull(saved.getNextAttemptAt());
        assertTrue(saved.getNextAttemptAt().isAfter(Instant.now()));

        ArgumentCaptor<DeliveryAttempt> attemptCaptor =
                ArgumentCaptor.forClass(DeliveryAttempt.class);
        verify(deliveryAttemptRepository).save(attemptCaptor.capture());
        assertEquals(AttemptOutcome.TEMPORARY_FAILURE, attemptCaptor.getValue().getOutcome());
    }

    @Test
    void sendStopsSchedulingAfterFinalAttempt() {
        ReflectionTestUtils.setField(service, "maxAttempts", 2);
        Notification notification = pendingNotification();
        notification.setAttemptCount(1);
        when(notificationRepository.findById(7L)).thenReturn(Optional.of(notification));
        when(emailService.send(any())).thenThrow(new RuntimeException("still down"));

        service.send(7L);

        ArgumentCaptor<Notification> notificationCaptor =
                ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertEquals(2, notificationCaptor.getValue().getAttemptCount());
        assertNull(notificationCaptor.getValue().getNextAttemptAt());

        ArgumentCaptor<DeliveryAttempt> attemptCaptor =
                ArgumentCaptor.forClass(DeliveryAttempt.class);
        verify(deliveryAttemptRepository).save(attemptCaptor.capture());
        assertEquals(AttemptOutcome.PERMANENT_FAILURE, attemptCaptor.getValue().getOutcome());
    }

    @Test
    void sendSkipsANotificationClaimedByAnotherWorker() {
        Notification notification = pendingNotification();
        notification.setNextAttemptAt(Instant.now().plusSeconds(60));
        when(notificationRepository.findById(7L)).thenReturn(Optional.of(notification));

        service.send(7L);

        verify(emailService, never()).send(any());
        verify(notificationRepository, never()).saveAndFlush(any());
        verify(deliveryAttemptRepository, never()).save(any());
    }

    @Test
    void prepareSuppressesMessagesWithoutARecipient() {
        EmailRequest request = request();
        request.setTo(null);

        Long id = service.prepare(null, "user.welcome", "welcome", request);

        assertNull(id);
        verify(emailService, never()).send(any());
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationStatus.SUPPRESSED, captor.getValue().getStatus());
        assertEquals("No recipient", captor.getValue().getFailureReason());
        assertTrue(captor.getValue().getEventKey().startsWith("auto:"));
    }

    private Notification pendingNotification() {
        Notification notification = new Notification();
        notification.setId(7L);
        notification.setSlug("pending-notification");
        notification.setEventKey("welcome:USER-1");
        notification.setEventType("user.welcome");
        notification.setRecipient("buyer@example.com");
        notification.setStatus(NotificationStatus.PENDING);
        notification.setAttemptCount(0);
        notification.setNextAttemptAt(Instant.now().minusSeconds(1));
        notification.setRequestPayload(payload(request()));
        return notification;
    }

    private String payload(EmailRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static EmailRequest request() {
        EmailRequest request = new EmailRequest();
        request.setTo("buyer@example.com");
        request.setSubject("Your receipt");
        request.setHtml("<p>Thank you</p>");
        request.setText("Thank you");
        request.setAttachments(
                List.of(
                        new EmailAttachment(
                                "receipt.txt",
                                "text/plain",
                                "pdf-bytes".getBytes(StandardCharsets.UTF_8))));
        return request;
    }
}
