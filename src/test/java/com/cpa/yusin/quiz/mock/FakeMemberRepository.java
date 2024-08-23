package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class FakeMemberRepository implements MemberRepository
{
    private final AtomicLong autoGeneratedId = new AtomicLong(0);
    private final List<MemberDomain> data = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Optional<MemberDomain> findByEmail(String email)
    {
        return data.stream()
                .filter(item -> item.getEmail().equals(email))
                .findAny();
    }

    @Override
    public boolean existsByEmail(String email)
    {
        return data.stream().anyMatch(item -> item.getEmail().equals(email));
    }

    @Override
    public MemberDomain save(MemberDomain member)
    {
        if(member.getId() == null || member.getId() == 0){
            MemberDomain newMember = MemberDomain.builder()
                    .id(autoGeneratedId.getAndIncrement())
                    .email(member.getEmail())
                    .password(member.getPassword())
                    .username(member.getUsername())
                    .platform(member.getPlatform())
                    .role(member.getRole())
                    .build();
            data.add(newMember);
            return newMember;

        } else {
            data.removeIf(item -> Objects.equals(item.getId(), member.getId()));
            data.add(member);
        }

        return member;
    }

    @Override
    public Optional<MemberDomain> findById(long id)
    {
        return data.stream()
                .filter(item -> item.getId().equals(id))
                .findAny();
    }

    @Override
    public void deleteById(long id)
    {
        data.removeIf(item -> item.getId().equals(id));
    }
}
