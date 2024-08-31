package com.cpa.yusin.quiz.member.service.port;

import com.cpa.yusin.quiz.member.domain.MemberDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberRepository
{
    Optional<MemberDomain> findByEmail(String email);

    Page<MemberDomain> findAllByKeyword(String keyword, Pageable pageable);

    boolean existsByEmail(String email);

    MemberDomain save(MemberDomain member);

    Optional<MemberDomain> findById(long id);

    void deleteById(long id);
}
