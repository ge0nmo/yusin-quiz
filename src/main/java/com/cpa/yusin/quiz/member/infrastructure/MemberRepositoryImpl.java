package com.cpa.yusin.quiz.member.infrastructure;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class MemberRepositoryImpl implements MemberRepository
{
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findByEmail(String email)
    {
        return memberJpaRepository.findByEmail(email);
    }

    @Override
    public Page<Member> findAllByKeyword(String keyword, Pageable pageable)
    {
        return memberJpaRepository.findAllByKeyword(keyword, pageable);
    }

    @Override
    public boolean existsByEmail(String email)
    {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public Member save(Member member)
    {
        return memberJpaRepository.save(member);
    }

    @Override
    public Optional<Member> findById(long id)
    {
        return memberJpaRepository.findById(id);
    }

    @Override
    public void deleteById(long id)
    {
        memberJpaRepository.deleteById(id);
    }
}
