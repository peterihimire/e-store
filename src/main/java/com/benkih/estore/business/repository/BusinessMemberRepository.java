package com.benkih.estore.business.repository;

import com.benkih.estore.business.entity.BusinessMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessMemberRepository extends JpaRepository<BusinessMember, Long> {

  Optional<BusinessMember> findByBusinessSlugAndUserSlug(
      String businessSlug,
      String userSlug
  );

  List<BusinessMember> findByUserSlug(
      String userSlug
  );

  List<BusinessMember> findByBusinessSlug(
      String businessString
  );

  boolean existsByBusinessSlugAndUserSlug(
      String businessSlug,
      String userSlug
  );
}
