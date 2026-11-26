package com.benkih.estore.business.repository;

import com.benkih.estore.business.entity.BusinessMember;
import com.benkih.estore.business.enums.MemberStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessMemberRepository extends JpaRepository<BusinessMember, Long> {

    Optional<BusinessMember> findByBusinessSlugAndUserSlug(String businessSlug, String userSlug);

    List<BusinessMember> findByUserSlug(String userSlug);

    List<BusinessMember> findByBusinessSlug(String businessString);

    boolean existsByBusinessSlugAndUserSlug(String businessSlug, String userSlug);

    Optional<BusinessMember> findFirstByUserIdAndStatus(Long userId, MemberStatus status);
}
