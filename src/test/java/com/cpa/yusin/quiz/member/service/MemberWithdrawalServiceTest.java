package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.member.service.port.MemberWithdrawalRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MemberWithdrawalServiceTest {
    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final MemberWithdrawalRepository withdrawalRepository = mock(MemberWithdrawalRepository.class);
    private final WithdrawnAuthorProvider withdrawnAuthorProvider = mock(WithdrawnAuthorProvider.class);
    private final MemberWithdrawalServiceImpl service =
            new MemberWithdrawalServiceImpl(memberRepository, withdrawalRepository, withdrawnAuthorProvider);

    @Test
    void withdrawLocksMemberAndUsesSharedAnonymousAuthor() {
        Member member = member(10L, "user@example.com");
        Member withdrawnAuthor = member(99L, Member.WITHDRAWN_AUTHOR_EMAIL);
        given(memberRepository.findByIdWithLock(10L)).willReturn(Optional.of(member));
        given(withdrawnAuthorProvider.getOrCreate()).willReturn(withdrawnAuthor);

        service.withdraw(10L);

        verify(withdrawalRepository).withdraw(10L, withdrawnAuthor);
    }

    @Test
    void withdrawRejectsMissingMemberWithoutDeletingAnything() {
        given(memberRepository.findByIdWithLock(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.withdraw(404L))
                .isInstanceOf(MemberException.class)
                .satisfies(exception -> assertThat(((MemberException) exception).getExceptionMessage())
                        .isEqualTo(ExceptionMessage.USER_NOT_FOUND));
        verify(withdrawalRepository, never()).withdraw(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void systemWithdrawalAuthorCannotWithdraw() {
        Member withdrawnAuthor = member(1L, Member.WITHDRAWN_AUTHOR_EMAIL);
        given(memberRepository.findByIdWithLock(1L)).willReturn(Optional.of(withdrawnAuthor));

        assertThatThrownBy(() -> service.withdraw(1L))
                .isInstanceOf(MemberException.class)
                .satisfies(exception -> assertThat(((MemberException) exception).getExceptionMessage())
                        .isEqualTo(ExceptionMessage.NO_AUTHORIZATION));
        verify(withdrawalRepository, never()).withdraw(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any());
    }

    private Member member(long id, String email) {
        return Member.builder()
                .id(id)
                .email(email)
                .password("encoded-password")
                .username("사용자")
                .platform(Platform.HOME)
                .role(Role.USER)
                .build();
    }
}
