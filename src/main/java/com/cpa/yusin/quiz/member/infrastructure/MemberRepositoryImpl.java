package com.cpa.yusin.quiz.member.infrastructure;

import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class MemberRepositoryImpl implements MemberRepository
{
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<MemberDomain> findByEmail(String email)
    {
        return memberJpaRepository.findByEmail(email)
                .map(Member::toDomain);
    }

    @Override
    public boolean existsByEmail(String email)
    {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public MemberDomain save(MemberDomain memberDomain)
    {
        return memberJpaRepository.save(Member.fromDomain(memberDomain))
                .toDomain();
    }

    @Override
    public Optional<MemberDomain> findById(Long id)
    {
        return memberJpaRepository.findById(id)
                .map(Member::toDomain);
    }
}