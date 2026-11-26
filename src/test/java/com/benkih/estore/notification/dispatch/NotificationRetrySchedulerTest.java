package com.benkih.estore.notification.dispatch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benkih.estore.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationRetrySchedulerTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationDispatchService dispatchService;

    private NotificationRetryScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NotificationRetryScheduler(notificationRepository, dispatchService);
        ReflectionTestUtils.setField(scheduler, "batchSize", 2);
    }

    @Test
    void dispatchesEveryDueNotification() {
        when(notificationRepository.findDueIds(
                        anyCollection(), any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(1L, 2L));

        scheduler.retryDueNotifications();

        verify(dispatchService).send(1L);
        verify(dispatchService).send(2L);
    }

    @Test
    void oneFailureDoesNotBlockOtherNotifications() {
        when(notificationRepository.findDueIds(
                        anyCollection(), any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(1L, 2L));
        doThrow(new RuntimeException("boom")).when(dispatchService).send(1L);

        scheduler.retryDueNotifications();

        verify(dispatchService).send(1L);
        verify(dispatchService).send(2L);
    }

    @Test
    void doesNothingWhenThereIsNoDueWork() {
        when(notificationRepository.findDueIds(
                        anyCollection(), any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of());

        scheduler.retryDueNotifications();

        verify(dispatchService, never()).send(any());
    }
}
