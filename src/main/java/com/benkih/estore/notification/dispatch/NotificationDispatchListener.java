package com.benkih.estore.notification.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class NotificationDispatchListener {

    private final NotificationDispatchService dispatchService;
    private final TaskExecutor taskExecutor;

    public NotificationDispatchListener(
            NotificationDispatchService dispatchService,
            @Qualifier("notificationTaskExecutor") TaskExecutor taskExecutor) {
        this.dispatchService = dispatchService;
        this.taskExecutor = taskExecutor;
    }

    /** The send is submitted only after commit, and never runs on the business request thread. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDispatch(NotificationDispatchEvent event) {
        try {
            taskExecutor.execute(() -> dispatchService.send(event.notificationId()));
        } catch (RuntimeException e) {
            // Do not break an already committed business transaction. The retry worker will recover
            // it.
            log.error(
                    "Unable to submit notification {} for dispatch; retry worker will pick it up",
                    event.notificationId(),
                    e);
        }
    }
}
