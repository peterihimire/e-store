package com.benkih.estore.notification.dispatch;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchListenerTest {

    @Mock private NotificationDispatchService dispatchService;
    @Mock private TaskExecutor taskExecutor;

    @Test
    void submitsDispatchToExecutorInsteadOfCallingItInline() {
        NotificationDispatchListener listener =
                new NotificationDispatchListener(dispatchService, taskExecutor);

        listener.onDispatch(new NotificationDispatchEvent(11L));

        ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(taskExecutor).execute(taskCaptor.capture());
        taskCaptor.getValue().run();
        verify(dispatchService).send(11L);
    }

    @Test
    void aFullExecutorDoesNotBreakTheCommittedTransaction() {
        NotificationDispatchListener listener =
                new NotificationDispatchListener(dispatchService, taskExecutor);
        doThrow(new TaskRejectedException("queue full")).when(taskExecutor).execute(any());

        assertDoesNotThrow(() -> listener.onDispatch(new NotificationDispatchEvent(12L)));
    }
}
