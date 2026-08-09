package com.cpa.yusin.quiz.wordpractice.service;

import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipantType;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeParticipantRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WordPracticeParticipantResolver {

    private final WordPracticeParticipantRepository participantRepository;
    private final WordPracticeParticipantCreator participantCreator;
    private final MemberRepository memberRepository;
    private final GuestTokenHasher guestTokenHasher;
    private final UuidHolder uuidHolder;

    public WordPracticeParticipantResolver(
            WordPracticeParticipantRepository participantRepository,
            WordPracticeParticipantCreator participantCreator,
            MemberRepository memberRepository,
            GuestTokenHasher guestTokenHasher,
            @Qualifier("systemUuidHolder") UuidHolder uuidHolder
    ) {
        this.participantRepository = participantRepository;
        this.participantCreator = participantCreator;
        this.memberRepository = memberRepository;
        this.guestTokenHasher = guestTokenHasher;
        this.uuidHolder = uuidHolder;
    }

    @Transactional(readOnly = true)
    public Optional<WordPracticeParticipant> resolve(Long memberId, String guestToken) {
        if (memberId != null) {
            return findMember(memberId);
        }
        if (guestToken == null) {
            return Optional.empty();
        }
        return findGuest(guestToken);
    }

    @Transactional
    public WordPracticeParticipantResolution createOrResolve(Long memberId, String guestToken) {
        if (memberId != null) {
            lockMember(memberId);
            return WordPracticeParticipantResolution.existing(getOrCreateMember(memberId));
        }

        if (guestToken != null) {
            return WordPracticeParticipantResolution.existing(getOrCreateGuest(guestToken));
        }

        String issuedGuestToken = uuidHolder.getRandom();
        return WordPracticeParticipantResolution.issued(
                getOrCreateGuest(issuedGuestToken),
                issuedGuestToken
        );
    }

    /**
     * 회원 데이터 쓰기는 회원 행을 먼저 잠가 탈퇴와 같은 순서로 직렬화한다.
     * 탈퇴가 먼저 커밋되면 잠금 조회가 회원을 찾지 못해 이후 participant/cycle을 만들 수 없다.
     */
    @Transactional
    public Optional<WordPracticeParticipant> resolveForWrite(Long memberId, String guestToken) {
        if (memberId != null) {
            lockMember(memberId);
            return findMember(memberId);
        }
        if (guestToken == null) {
            return Optional.empty();
        }
        return findGuest(guestToken);
    }

    private void lockMember(Long memberId) {
        memberRepository.findByIdWithLock(memberId)
                .orElseThrow(() -> new MemberException(ExceptionMessage.USER_NOT_FOUND));
    }

    private Optional<WordPracticeParticipant> findMember(Long memberId) {
        return participantRepository.findByTypeAndOwnerKey(
                WordPracticeParticipantType.MEMBER,
                String.valueOf(memberId)
        );
    }

    private Optional<WordPracticeParticipant> findGuest(String guestToken) {
        return participantRepository.findByTypeAndOwnerKey(
                WordPracticeParticipantType.GUEST,
                guestTokenHasher.hash(guestToken)
        );
    }

    private WordPracticeParticipant getOrCreateMember(Long memberId) {
        String ownerKey = String.valueOf(memberId);
        return participantRepository.findByTypeAndOwnerKey(WordPracticeParticipantType.MEMBER, ownerKey)
                .orElseGet(() -> saveOrFindExisting(WordPracticeParticipant.member(memberId)));
    }

    private WordPracticeParticipant getOrCreateGuest(String guestToken) {
        String ownerKey = guestTokenHasher.hash(guestToken);
        return participantRepository.findByTypeAndOwnerKey(WordPracticeParticipantType.GUEST, ownerKey)
                .orElseGet(() -> saveOrFindExisting(WordPracticeParticipant.guest(ownerKey)));
    }

    private WordPracticeParticipant saveOrFindExisting(WordPracticeParticipant participant) {
        return participantCreator.create(participant);
    }
}
