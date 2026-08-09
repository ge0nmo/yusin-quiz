package com.cpa.yusin.quiz.member.integration;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.controller.port.MemberWithdrawalService;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.member.service.dto.SocialProfile;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipantType;
import com.cpa.yusin.quiz.wordpractice.infrastructure.WordPracticeParticipantJpaRepository;
import com.cpa.yusin.quiz.wordpractice.service.WordPracticeParticipantResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(TeardownExtension.class)
@SpringBootTest
class MemberWithdrawalConcurrencyTest {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberWithdrawalService memberWithdrawalService;
    @Autowired
    private WordPracticeParticipantResolver participantResolver;
    @Autowired
    private WordPracticeParticipantJpaRepository participantRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private JwtService jwtService;

    @Test
    void concurrentWithdrawalsCreateOneSharedAnonymousAuthor() throws Exception {
        Member first = createMember("first-withdrawal@example.com");
        Member second = createMember("second-withdrawal@example.com");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Void> firstFuture = concurrentWithdrawal(executor, ready, start, first.getId());
            Future<Void> secondFuture = concurrentWithdrawal(executor, ready, start, second.getId());
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            firstFuture.get(5, TimeUnit.SECONDS);
            secondFuture.get(5, TimeUnit.SECONDS);

            assertThat(memberRepository.findById(first.getId())).isEmpty();
            assertThat(memberRepository.findById(second.getId())).isEmpty();
            assertThat(memberRepository.findWithdrawnAuthor()).isPresent();
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void refreshWaitsForWithdrawalThenRejectsDeletedAccount() throws Exception {
        Member member = createMember("refresh-race@example.com");
        String refreshToken = jwtService.createRefreshToken(member.getEmail(), member.getId());
        CountDownLatch withdrawalApplied = new CountDownLatch(1);
        CountDownLatch allowWithdrawalCommit = new CountDownLatch(1);
        CountDownLatch refreshStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Void> withdrawalFuture = heldWithdrawal(
                    executor, member.getId(), withdrawalApplied, allowWithdrawalCommit);
            assertThat(withdrawalApplied.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Void> refreshFuture = executor.submit(() -> {
                refreshStarted.countDown();
                authenticationService.refreshAccessToken(refreshToken);
                return null;
            });
            assertThat(refreshStarted.await(5, TimeUnit.SECONDS)).isTrue();
            assertBlocked(refreshFuture);

            allowWithdrawalCommit.countDown();
            withdrawalFuture.get(5, TimeUnit.SECONDS);
            assertThatThrownBy(() -> refreshFuture.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(MemberException.class);
        } finally {
            allowWithdrawalCommit.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void socialLoginWaitsForWithdrawalThenCreatesFreshBoundAccount() throws Exception {
        Member member = createMember("social-race@example.com");
        CountDownLatch withdrawalApplied = new CountDownLatch(1);
        CountDownLatch allowWithdrawalCommit = new CountDownLatch(1);
        CountDownLatch loginStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Void> withdrawalFuture = heldWithdrawal(
                    executor, member.getId(), withdrawalApplied, allowWithdrawalCommit);
            assertThat(withdrawalApplied.await(5, TimeUnit.SECONDS)).isTrue();

            Future<LoginResponse> loginFuture = executor.submit(() -> {
                loginStarted.countDown();
                return authenticationService.socialLogin(SocialProfile.builder()
                        .email(member.getEmail())
                        .name("new social user")
                        .platform(Platform.GOOGLE)
                        .build());
            });
            assertThat(loginStarted.await(5, TimeUnit.SECONDS)).isTrue();
            assertBlocked(loginFuture);

            allowWithdrawalCommit.countDown();
            withdrawalFuture.get(5, TimeUnit.SECONDS);
            LoginResponse response = loginFuture.get(5, TimeUnit.SECONDS);

            assertThat(response.getId()).isNotEqualTo(member.getId());
            Member freshMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
            assertThat(freshMember.getId()).isEqualTo(response.getId());
            assertThat(jwtService.isValidToken(
                    response.getAccessToken(), new MemberDetails(freshMember, java.util.Map.of()))).isTrue();
        } finally {
            allowWithdrawalCommit.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void withdrawalWaitsForParticipantCreationThenDeletesIt() throws Exception {
        Member member = createMember("word-first@example.com");
        CountDownLatch participantCreated = new CountDownLatch(1);
        CountDownLatch allowWordCommit = new CountDownLatch(1);
        CountDownLatch withdrawalStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Void> wordFuture = executor.submit(() -> {
                transactionTemplate.executeWithoutResult(status -> {
                    participantResolver.createOrResolve(member.getId(), null);
                    participantCreated.countDown();
                    await(allowWordCommit);
                });
                return null;
            });
            assertThat(participantCreated.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Void> withdrawalFuture = executor.submit(() -> {
                withdrawalStarted.countDown();
                memberWithdrawalService.withdraw(member.getId());
                return null;
            });
            assertThat(withdrawalStarted.await(5, TimeUnit.SECONDS)).isTrue();
            assertBlocked(withdrawalFuture);

            allowWordCommit.countDown();
            wordFuture.get(5, TimeUnit.SECONDS);
            withdrawalFuture.get(5, TimeUnit.SECONDS);

            assertThat(memberRepository.findById(member.getId())).isEmpty();
            assertThat(participantRepository.findByTypeAndOwnerKey(
                    WordPracticeParticipantType.MEMBER, String.valueOf(member.getId()))).isEmpty();
        } finally {
            allowWordCommit.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void participantCreationWaitsForWithdrawalThenFails() throws Exception {
        Member member = createMember("withdrawal-first@example.com");
        CountDownLatch withdrawalApplied = new CountDownLatch(1);
        CountDownLatch allowWithdrawalCommit = new CountDownLatch(1);
        CountDownLatch wordStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Void> withdrawalFuture = executor.submit(() -> {
                transactionTemplate.executeWithoutResult(status -> {
                    memberWithdrawalService.withdraw(member.getId());
                    withdrawalApplied.countDown();
                    await(allowWithdrawalCommit);
                });
                return null;
            });
            assertThat(withdrawalApplied.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Void> wordFuture = executor.submit(() -> {
                wordStarted.countDown();
                participantResolver.createOrResolve(member.getId(), null);
                return null;
            });
            assertThat(wordStarted.await(5, TimeUnit.SECONDS)).isTrue();
            assertBlocked(wordFuture);

            allowWithdrawalCommit.countDown();
            withdrawalFuture.get(5, TimeUnit.SECONDS);

            assertThatThrownBy(() -> wordFuture.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(MemberException.class);
            assertThat(memberRepository.findById(member.getId())).isEmpty();
            assertThat(participantRepository.findByTypeAndOwnerKey(
                    WordPracticeParticipantType.MEMBER, String.valueOf(member.getId()))).isEmpty();
        } finally {
            allowWithdrawalCommit.countDown();
            executor.shutdownNow();
        }
    }

    private Member createMember(String email) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password("encoded-password")
                .username(email)
                .platform(Platform.GOOGLE)
                .role(Role.USER)
                .build());
    }

    private Future<Void> concurrentWithdrawal(
            ExecutorService executor,
            CountDownLatch ready,
            CountDownLatch start,
            long memberId
    ) {
        return executor.submit(() -> {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for concurrent withdrawal start");
            }
            memberWithdrawalService.withdraw(memberId);
            return null;
        });
    }

    private Future<Void> heldWithdrawal(
            ExecutorService executor,
            long memberId,
            CountDownLatch withdrawalApplied,
            CountDownLatch allowWithdrawalCommit
    ) {
        return executor.submit(() -> {
            transactionTemplate.executeWithoutResult(status -> {
                memberWithdrawalService.withdraw(memberId);
                withdrawalApplied.countDown();
                await(allowWithdrawalCommit);
            });
            return null;
        });
    }

    private void assertBlocked(Future<?> future) {
        assertThatThrownBy(() -> future.get(250, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for test transaction release");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for test transaction release", exception);
        }
    }
}
