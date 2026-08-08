package com.cpa.yusin.quiz.wordpractice.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.WordPracticeException;
import com.cpa.yusin.quiz.mock.FakeUuidHolder;
import com.cpa.yusin.quiz.mock.FakeClockHolder;
import com.cpa.yusin.quiz.mock.FakeWordPracticeParticipantRepository;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipantType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordPracticeParticipantResolverTest {

    private static final String GUEST_TOKEN = "3f2504e0-4f89-41d3-9a0c-0305e82c3301";
    private static final String OTHER_GUEST_TOKEN = "550e8400-e29b-41d4-a716-446655440000";
    private FakeWordPracticeParticipantRepository repository;
    private GuestTokenHasher guestTokenHasher;
    private WordPracticeParticipantResolver resolver;

    @BeforeEach
    void setUp() {
        repository = new FakeWordPracticeParticipantRepository();
        guestTokenHasher = new GuestTokenHasher();
        resolver = new WordPracticeParticipantResolver(
                repository,
                new WordPracticeParticipantCreator(repository, new FakeClockHolder()),
                guestTokenHasher,
                new FakeUuidHolder(GUEST_TOKEN)
        );
    }

    @Test
    void sameMemberResolvesToTheSameParticipant() {
        var first = resolver.createOrResolve(42L, GUEST_TOKEN);
        var second = resolver.createOrResolve(42L, OTHER_GUEST_TOKEN);

        assertThat(first.participant().getId()).isEqualTo(second.participant().getId());
        assertThat(first.participant().getType()).isEqualTo(WordPracticeParticipantType.MEMBER);
        assertThat(repository.size()).isOne();
    }

    @Test
    void guestTokenIsHashedAndKeepsGuestsSeparateFromMembers() {
        var guest = resolver.createOrResolve(null, GUEST_TOKEN);
        var sameGuest = resolver.createOrResolve(null, GUEST_TOKEN);
        var otherGuest = resolver.createOrResolve(null, OTHER_GUEST_TOKEN);
        var member = resolver.createOrResolve(42L, GUEST_TOKEN);

        assertThat(guest.participant().getId()).isEqualTo(sameGuest.participant().getId());
        assertThat(otherGuest.participant().getId()).isNotEqualTo(guest.participant().getId());
        assertThat(member.participant().getId()).isNotEqualTo(guest.participant().getId());
        assertThat(guest.participant().getOwnerKey())
                .isEqualTo(guestTokenHasher.hash(GUEST_TOKEN))
                .isNotEqualTo(GUEST_TOKEN)
                .hasSize(64);
    }

    @Test
    void readOnlyResolveDoesNotCreateParticipantOrIssueToken() {
        assertThat(resolver.resolve(null, null)).isEmpty();
        assertThat(resolver.resolve(null, GUEST_TOKEN)).isEmpty();
        assertThat(repository.size()).isZero();
    }

    @Test
    void createWithoutGuestTokenIssuesTheUuidHolderValue() {
        var resolution = resolver.createOrResolve(null, null);

        assertThat(resolution.issuedGuestToken()).contains(GUEST_TOKEN);
        assertThat(resolution.participant().getOwnerKey()).isEqualTo(guestTokenHasher.hash(GUEST_TOKEN));
    }

    @Test
    void invalidGuestTokenIsRejectedWithoutLeakingItsValue() {
        assertThatThrownBy(() -> resolver.createOrResolve(null, "not-a-uuid"))
                .isInstanceOf(WordPracticeException.class)
                .satisfies(exception -> assertThat(((WordPracticeException) exception).getExceptionMessage())
                        .isEqualTo(ExceptionMessage.WORD_PRACTICE_INVALID_GUEST_TOKEN));
        assertThat(repository.size()).isZero();
    }
}
