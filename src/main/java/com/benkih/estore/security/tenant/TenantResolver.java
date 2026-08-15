package com.benkih.estore.security.tenant;

import com.benkih.estore.business.enums.MemberStatus;
import com.benkih.estore.business.repository.BusinessMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantResolver {
  private final BusinessMemberRepository businessMemberRepository;

  public Long resolve(Long userId) {

    return businessMemberRepository.findFirstByUserIdAndStatus(
            userId,
            MemberStatus.ACTIVE
        )
        .map(member -> member.getBusiness().getId())
        .orElse(null);
  }
}
