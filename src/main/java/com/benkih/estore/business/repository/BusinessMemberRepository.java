package com.benkih.estore.business.repository;

import com.benkih.estore.business.entity.BusinessMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessMemberRepository extends JpaRepository<BusinessMember, Long> {

  Optional<BusinessMember> findByBusinessIdAndUserId(
      Long businessId,
      Long userId
  );

  List<BusinessMember> findByUserId(
      Long userId
  );

  List<BusinessMember> findByBusinessId(
      Long businessId
  );

  boolean existsByBusinessIdAndUserId(
      Long businessId,
      Long userId
  );
}
