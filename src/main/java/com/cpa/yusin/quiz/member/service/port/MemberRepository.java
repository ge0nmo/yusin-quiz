package com.cpa.yusin.quiz.member.service.port;

import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.infrastructure.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository
{
    Optional<MemberDomain> findByEmail(String email);

    boolean existsByEmail(String email);

    MemberDomain save(MemberDomain member);

    Optional<MemberDomain> findById(long id);

    void deleteById(long id);
}
