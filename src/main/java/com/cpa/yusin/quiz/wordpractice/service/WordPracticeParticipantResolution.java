package com.cpa.yusin.quiz.wordpractice.service;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;

import java.util.Optional;

public record WordPracticeParticipantResolution(
        WordPracticeParticipant participant,
        Optional<String> issuedGuestToken
) {

    public static WordPracticeParticipantResolution existing(WordPracticeParticipant participant) {
        return new WordPracticeParticipantResolution(participant, Optional.empty());
    }

    public static WordPracticeParticipantResolution issued(WordPracticeParticipant participant, String guestToken) {
        return new WordPracticeParticipantResolution(participant, Optional.of(guestToken));
    }

    @Override
    public String toString() {
        return "WordPracticeParticipantResolution[participantId=" + participant.getId()
                + ", issuedGuestTokenPresent=" + issuedGuestToken.isPresent() + "]";
    }
}
