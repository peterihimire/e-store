package com.benkih.estore.notification.repository;

import com.benkih.estore.notification.entity.Notification;
import com.benkih.estore.notification.enums.NotificationStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByEventKey(String eventKey);

    boolean existsByEventKey(String eventKey);

    @Query(
            """
      select n.id from Notification n
      where n.status in :statuses
        and n.nextAttemptAt <= :now
      order by n.nextAttemptAt asc
      """)
    List<Long> findDueIds(
            @Param("statuses") Collection<NotificationStatus> statuses,
            @Param("now") Instant now,
            Pageable pageable);
}
