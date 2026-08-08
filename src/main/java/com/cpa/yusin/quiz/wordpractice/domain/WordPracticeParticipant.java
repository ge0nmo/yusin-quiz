package com.cpa.yusin.quiz.wordpractice.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "word_practice_participant", uniqueConstraints = @UniqueConstraint(
        name = "uk_word_practice_participant_type_owner_key",
        columnNames = {"type", "owner_key"}
))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordPracticeParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private WordPracticeParticipantType type;

    @Column(name = "owner_key", nullable = false, updatable = false, length = 64)
    private String ownerKey;

    public static WordPracticeParticipant member(Long memberId) {
        return create(WordPracticeParticipantType.MEMBER, String.valueOf(memberId));
    }

    public static WordPracticeParticipant guest(String guestTokenHash) {
        return create(WordPracticeParticipantType.GUEST, guestTokenHash);
    }

    private static WordPracticeParticipant create(WordPracticeParticipantType type, String ownerKey) {
        return WordPracticeParticipant.builder()
                .type(type)
                .ownerKey(ownerKey)
                .build();
    }
}
