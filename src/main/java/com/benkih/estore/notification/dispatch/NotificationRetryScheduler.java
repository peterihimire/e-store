package com.benkih.estore.notification.dispatch;

import com.benkih.estore.notification.enums.NotificationStatus;
import com.benkih.estore.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRetryScheduler {

    private final NotificationRepository notificationRepository;
    private final NotificationDispatchService dispatchService;

    @Value("${app.notification.retry-batch-size:50}")
    private int batchSize;

    @Scheduled(
            fixedDelayString = "${app.notification.scan-delay-ms:30000}",
            initialDelayString = "${app.notification.scan-delay-ms:30000}")
    public void retryDueNotifications() {
        List<Long> ids =
                notificationRepository.findDueIds(
                        List.of(NotificationStatus.PENDING, NotificationStatus.FAILED),
                        Instant.now(),
                        PageRequest.of(0, batchSize));

        if (ids.isEmpty()) {
            return;
        }

        log.debug("Retrying {} due notifications", ids.size());
        for (Long id : ids) {
            try {
                dispatchService.send(id);
            } catch (Exception e) {
                log.error("Retry worker failed for notification {}", id, e);
            }
        }
    }
}
