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
public class MemberRepositoryImpl implements MemberRepository {
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email, Member.WITHDRAWN_AUTHOR_EMAIL);
    }

    @Override
    public Optional<Member> findByEmailWithLock(String email) {
        return memberJpaRepository.findByEmailWithLock(email, Member.WITHDRAWN_AUTHOR_EMAIL);
    }

    @Override
    public Optional<Member> findWithdrawnAuthor() {
        return memberJpaRepository.findWithdrawnAuthor(Member.WITHDRAWN_AUTHOR_EMAIL);
    }

    @Override
    public Optional<Member> findWithdrawnAuthorWithLock() {
        return memberJpaRepository.findWithdrawnAuthorWithLock(Member.WITHDRAWN_AUTHOR_EMAIL);
    }

    @Override
    public Page<Member> findAllByKeyword(String keyword, Pageable pageable) {
        return memberJpaRepository.findAllByKeyword(keyword, Member.WITHDRAWN_AUTHOR_EMAIL, pageable);
    }

    @Override
    public Page<Member> findAllByKeywordAndAdminNot(String keyword, Pageable pageable) {
        return memberJpaRepository.findAllByKeywordAndAdminNot(keyword, Member.WITHDRAWN_AUTHOR_EMAIL, pageable);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return memberJpaRepository.existsByUsername(username);
    }

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    @Override
    public Member saveAndFlush(Member member) {
        return memberJpaRepository.saveAndFlush(member);
    }

    @Override
    public Optional<Member> findById(long id) {
        return memberJpaRepository.findById(id);
    }

    @Override
    public Optional<Member> findByIdWithLock(long id) {
        return memberJpaRepository.findByIdWithLock(id);
    }

    @Override
    public void deleteById(long id) {
        memberJpaRepository.deleteById(id);
    }

    @Override
    public Member getReferenceById(long id) {
        return memberJpaRepository.getReferenceById(id);
    }
}
