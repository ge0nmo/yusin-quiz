package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class FakeMemberRepository implements MemberRepository
{
    private final AtomicLong autoGeneratedId = new AtomicLong(1);
    private final List<Member> data = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Optional<Member> findByEmail(String email)
    {
        return data.stream()
                .filter(item -> item.getEmail().equals(email))
                .findAny();
    }

    @Override
    public Page<Member> findAllByKeyword(String keyword, Pageable pageable)
    {
        List<Member> result = data.stream()
                .filter(member -> !StringUtils.hasLength(keyword) ||
                        member.getEmail().toLowerCase().contains(keyword) ||
                        member.getUsername().toLowerCase().contains(keyword))
                .sorted(Comparator.comparing(Member::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(pageable.getPageSize())
                .skip(pageable.getOffset())
                .toList();

        return new PageImpl<>(result, pageable, result.size());
    }

    @Override
    public Page<Member> findAllByKeywordAndAdminNot(String keyword, Pageable pageable)
    {
        List<Member> result = data.stream()
                .filter(member -> (!StringUtils.hasLength(keyword) ||
                        member.getEmail().toLowerCase().contains(keyword) ||
                        member.getUsername().toLowerCase().contains(keyword)) &&
                        !member.getRole().equals(Role.ADMIN))
                .sorted(Comparator.comparing(Member::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(pageable.getPageSize())
                .skip(pageable.getOffset())
                .toList();

        return new PageImpl<>(result, pageable, result.size());
    }

    @Override
    public boolean existsByEmail(String email)
    {
        return data.stream().anyMatch(item -> item.getEmail().equals(email));
    }

    @Override
    public Member save(Member member)
    {
        if(member.getId() == null || member.getId() == 0){
            Member newMember = Member.builder()
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
    public Optional<Member> findById(long id)
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
