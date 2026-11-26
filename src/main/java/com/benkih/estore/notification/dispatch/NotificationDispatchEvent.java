package com.benkih.estore.notification.dispatch;

/** Published after a notification row has been written so the send can wait for commit. */
public record NotificationDispatchEvent(Long notificationId) {}
