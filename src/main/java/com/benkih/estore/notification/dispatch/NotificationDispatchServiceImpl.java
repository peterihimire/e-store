package com.benkih.estore.notification.dispatch;

import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.service.EmailService;
import com.benkih.estore.notification.entity.DeliveryAttempt;
import com.benkih.estore.notification.entity.Notification;
import com.benkih.estore.notification.enums.AttemptOutcome;
import com.benkih.estore.notification.enums.NotificationStatus;
import com.benkih.estore.notification.repository.DeliveryAttemptRepository;
import com.benkih.estore.notification.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchServiceImpl implements NotificationDispatchService {

    private final NotificationRepository notificationRepository;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Value("${app.email.provider:unknown}")
    private String provider;

    @Value("${app.notification.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.notification.retry-delay-ms:500}")
    private long retryDelayMs;

    @Value("${app.notification.claim-lease-ms:60000}")
    private long claimLeaseMs;

    /**
     * Writes the outbox row in the caller's transaction. Deliberately not REQUIRES_NEW: rolling
     * back the business operation must also roll back the notification.
     */
    @Override
    public Long prepare(String eventKey, String eventType, String template, EmailRequest request) {
        if (request == null) {
            log.warn("Ignoring null email request for event {}", eventType);
            return null;
        }

        String key = StringUtils.hasText(eventKey) ? eventKey : contentKey(request);

        Notification notification;
        Optional<Notification> existing = notificationRepository.findByEventKey(key);
        if (existing.isPresent()) {
            Notification current = existing.get();
            if (current.getStatus() == NotificationStatus.SENT) {
                log.info(
                        "Event {} already sent (notification {}). Skipping.",
                        eventType,
                        current.getSlug());
                return null;
            }
            if (current.getAttemptCount() >= maxAttempts) {
                log.warn(
                        "Event {} exhausted {} delivery attempts. Skipping.",
                        eventType,
                        current.getAttemptCount());
                return null;
            }
            notification = current;
        } else {
            notification = buildNotification(key, eventType, template);
        }

        if (!StringUtils.hasText(request.getTo())) {
            notification.setStatus(NotificationStatus.SUPPRESSED);
            notification.setFailureReason("No recipient");
            notification.setNextAttemptAt(null);
            notification.setRequestPayload(null);
            notificationRepository.save(notification);
            log.warn("Notification {} suppressed — no recipient", notification.getSlug());
            return null;
        }

        notification.setStatus(NotificationStatus.PENDING);
        notification.setFailureReason(null);
        notification.setRecipient(request.getTo());
        notification.setSubject(request.getSubject());
        notification.setBody(request.getHtml() != null ? request.getHtml() : request.getText());
        notification.setRequestPayload(serialize(request));
        // Due immediately: if the after-commit callback never runs, the retry worker recovers it.
        notification.setNextAttemptAt(Instant.now());

        Notification saved = notificationRepository.save(notification);
        return saved.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void send(Long notificationId) {
        if (notificationId == null) {
            return;
        }

        Optional<Notification> found = notificationRepository.findById(notificationId);
        if (found.isEmpty()) {
            log.warn("Notification {} no longer exists. Skipping.", notificationId);
            return;
        }

        Notification notification = found.get();
        if (notification.getStatus() == NotificationStatus.SENT
                || notification.getStatus() == NotificationStatus.SUPPRESSED
                || notification.getAttemptCount() >= maxAttempts) {
            return;
        }

        if (!claim(notification)) {
            log.debug(
                    "Notification {} was claimed by another worker. Skipping.",
                    notification.getSlug());
            return;
        }

        int attemptNumber = notification.getAttemptCount() + 1;
        long started = System.currentTimeMillis();
        String providerMessageId = null;

        try {
            EmailRequest request = rebuildRequest(notification);
            providerMessageId = emailService.send(request);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setFailureReason(null);
            notification.setProviderMessageId(providerMessageId);
            notification.setNextAttemptAt(null);
            notification.setAttemptCount(attemptNumber);

            recordAttempt(
                    notification,
                    attemptNumber,
                    AttemptOutcome.SUCCESS,
                    null,
                    providerMessageId,
                    elapsed(started));

            log.info(
                    "Notification {} sent on attempt {}/{} (provider id {})",
                    notification.getSlug(),
                    attemptNumber,
                    maxAttempts,
                    providerMessageId);
        } catch (Exception e) {
            String error =
                    abbreviate(
                            e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage(),
                            2000);
            boolean terminal = attemptNumber >= maxAttempts;

            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(error);
            notification.setAttemptCount(attemptNumber);
            notification.setNextAttemptAt(
                    terminal ? null : Instant.now().plusMillis(backoffMillis(attemptNumber)));

            recordAttempt(
                    notification,
                    attemptNumber,
                    terminal ? AttemptOutcome.PERMANENT_FAILURE : AttemptOutcome.TEMPORARY_FAILURE,
                    error,
                    null,
                    elapsed(started));

            log.warn(
                    "Email attempt {}/{} failed for notification {}: {}",
                    attemptNumber,
                    maxAttempts,
                    notification.getSlug(),
                    error);
        }

        notificationRepository.save(notification);
    }

    private boolean claim(Notification notification) {
        Instant now = Instant.now();
        if (notification.getNextAttemptAt() != null
                && notification.getNextAttemptAt().isAfter(now)) {
            return false;
        }

        notification.setNextAttemptAt(now.plusMillis(claimLeaseMs));
        try {
            // Flush inside the claim so a competing worker loses on the optimistic lock
            // immediately.
            notificationRepository.saveAndFlush(notification);
            return true;
        } catch (OptimisticLockingFailureException e) {
            log.debug("Lost claim race for notification {}", notification.getSlug());
            return false;
        }
    }

    private Notification buildNotification(String eventKey, String eventType, String template) {
        Notification notification = new Notification();
        notification.setEventKey(eventKey);
        notification.setEventType(eventType);
        notification.setTemplate(template);
        notification.setChannel("EMAIL");
        notification.setStatus(NotificationStatus.PENDING);
        return notification;
    }

    private void recordAttempt(
            Notification notification,
            int attemptNumber,
            AttemptOutcome outcome,
            String error,
            String providerMessageId,
            long durationMs) {

        DeliveryAttempt attempt = new DeliveryAttempt();
        attempt.setNotification(notification);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setOutcome(outcome);
        attempt.setProvider(provider);
        attempt.setProviderMessageId(providerMessageId);
        attempt.setErrorMessage(error);
        attempt.setDurationMs(durationMs);
        attempt.setAttemptedAt(Instant.now());

        deliveryAttemptRepository.save(attempt);
    }

    private String serialize(EmailRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to persist email request", e);
        }
    }

    private EmailRequest rebuildRequest(Notification notification) {
        if (StringUtils.hasText(notification.getRequestPayload())) {
            try {
                return objectMapper.readValue(notification.getRequestPayload(), EmailRequest.class);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "Unable to rebuild email request for " + notification.getSlug(), e);
            }
        }

        // Compatibility with rows written before the payload column existed.
        EmailRequest request = new EmailRequest();
        request.setTo(notification.getRecipient());
        request.setSubject(notification.getSubject());
        request.setHtml(notification.getBody());
        return request;
    }

    private long backoffMillis(int attemptNumber) {
        return retryDelayMs * attemptNumber;
    }

    private String contentKey(EmailRequest request) {
        String source =
                request.getTo()
                        + "|"
                        + request.getSubject()
                        + "|"
                        + request.getHtml()
                        + "|"
                        + request.getText();

        try {
            byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(source.getBytes(StandardCharsets.UTF_8));
            return "auto:" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static long elapsed(long started) {
        return System.currentTimeMillis() - started;
    }

    private static String abbreviate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }
}
