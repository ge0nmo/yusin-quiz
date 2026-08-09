package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_BOOTSTRAP_LOGIN_ID:}")
    private String loginId;
    @Value("${ADMIN_BOOTSTRAP_PASSWORD:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (memberRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        if (!StringUtils.hasText(loginId) || !StringUtils.hasText(password)) {
            log.warn("관리자 계정이 없습니다. ADMIN_BOOTSTRAP_LOGIN_ID/PASSWORD를 설정하세요.");
            return;
        }
        memberRepository.save(new Member(loginId.trim(), passwordEncoder.encode(password), Role.ADMIN));
        log.info("최초 관리자 계정을 생성했습니다: loginId={}", loginId.trim());
    }
}
