package com.benkih.estore.audit.repository;

import com.benkih.estore.audit.entity.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {

  Optional<ApiLog> findBySlug(String slug);

  List<ApiLog> findByMethod(String method);

  List<ApiLog> findByStatusCode(Integer statusCode);

  List<ApiLog> findByEndpointContaining(String endpoint);

  List<ApiLog> findByCreatedAtBetween(
      LocalDateTime from,
      LocalDateTime to
  );
}
