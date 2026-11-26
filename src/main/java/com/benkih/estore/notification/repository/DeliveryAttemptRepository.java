package com.benkih.estore.notification.repository;

import com.benkih.estore.notification.entity.DeliveryAttempt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, Long> {

    long countByNotificationId(Long notificationId);

    List<DeliveryAttempt> findByNotificationIdOrderByAttemptNumberAsc(Long notificationId);
}
