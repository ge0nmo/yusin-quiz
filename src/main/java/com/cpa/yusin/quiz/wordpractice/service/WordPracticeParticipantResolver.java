package com.cpa.yusin.quiz.wordpractice.service;

import com.cpa.yusin.quiz.common.service.UuidHolder;
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
    private final GuestTokenHasher guestTokenHasher;
    private final UuidHolder uuidHolder;

    public WordPracticeParticipantResolver(
            WordPracticeParticipantRepository participantRepository,
            WordPracticeParticipantCreator participantCreator,
            GuestTokenHasher guestTokenHasher,
            @Qualifier("systemUuidHolder") UuidHolder uuidHolder
    ) {
        this.participantRepository = participantRepository;
        this.participantCreator = participantCreator;
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
