package com.benkih.estore.notification.dispatch;

import com.benkih.estore.email.dto.EmailRequest;

public interface NotificationDispatchService {
    Long prepare(String eventKey, String eventType, String template, EmailRequest request);

    void send(Long notificationId);
}
