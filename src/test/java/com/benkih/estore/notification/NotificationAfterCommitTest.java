package com.benkih.estore.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.benkih.estore.email.service.EmailService;
import com.benkih.estore.notification.entity.Notification;
import com.benkih.estore.notification.enums.NotificationStatus;
import com.benkih.estore.notification.repository.DeliveryAttemptRepository;
import com.benkih.estore.notification.repository.NotificationRepository;
import com.benkih.estore.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@ActiveProfiles("test")
@SpringBootTest(
        properties = {
            // Own database: other cached test contexts keep the default testdb alive,
            // which would make the schema initializer see tables that already exist.
            "spring.datasource.url=jdbc:h2:mem:notification-after-commit",
            "spring.flyway.enabled=false",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:notification-schema.sql"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NotificationAfterCommitTest {

    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private NotificationService notificationService;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private DeliveryAttemptRepository deliveryAttemptRepository;

    @MockitoBean private EmailService emailService;

    @Test
    void sendsOnlyAfterTheBusinessTransactionCommits() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        transaction.executeWithoutResult(status -> notificationService.sendWelcomeEmail(user()));

        verify(emailService, timeout(5000)).send(any());
        Notification notification = awaitSentNotification();
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertEquals(1, notification.getAttemptCount());
        assertEquals("buyer@example.com", notification.getRecipient());
    }

    @Test
    void doesNotSendOrPersistWhenTheBusinessTransactionRollsBack() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                transaction.executeWithoutResult(
                                        status -> {
                                            notificationService.sendWelcomeEmail(user());
                                            throw new RuntimeException("rollback");
                                        }));

        assertEquals("rollback", thrown.getMessage());
        verify(emailService, never()).send(any());
        assertEquals(0, notificationRepository.count());
        assertEquals(0, deliveryAttemptRepository.count());
    }

    /**
     * The async listener persists SENT just after the mocked send returns, so poll for the final
     * status instead of asserting at the instant the mock is invoked.
     */
    private Notification awaitSentNotification() {
        long deadline = System.currentTimeMillis() + 5000;
        try {
            while (System.currentTimeMillis() < deadline) {
                Notification sent =
                        notificationRepository.findAll().stream()
                                .filter(n -> n.getStatus() == NotificationStatus.SENT)
                                .findFirst()
                                .orElse(null);
                if (sent != null) {
                    return sent;
                }
                Thread.sleep(25);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        throw new AssertionError("Notification was not persisted as SENT within 5s");
    }

    private static User user() {
        User user = new User();
        user.setSlug("user-1");
        user.setFirstName("Test");
        user.setLastName("Buyer");
        user.setEmail("buyer@example.com");
        return user;
    }
}
