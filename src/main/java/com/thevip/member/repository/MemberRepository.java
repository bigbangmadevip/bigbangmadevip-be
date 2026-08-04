package com.thevip.member.repository;

import com.thevip.member.entity.Member;
import com.thevip.member.entity.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);
}
