package com.cpa.yusin.quiz.member.service.port;

import com.cpa.yusin.quiz.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberRepository
{
    Optional<Member> findByEmail(String email);

    Page<Member> findAllByKeyword(String keyword, Pageable pageable);

    Page<Member>findAllByKeywordAndAdminNot(String keyword, Pageable pageable);

    boolean existsByEmail(String email);

    Member save(Member member);

    Optional<Member> findById(long id);

    void deleteById(long id);
}
