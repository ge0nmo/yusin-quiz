package com.cpa.yusin.quiz.member;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.infrastructure.MemberRepository;
import com.cpa.yusin.quiz.member.service.AdminBootstrap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminBootstrapTest {
    @Test
    void createsAdminOnlyWhenNoAdminExistsAndEnvironmentValuesArePresent() throws Exception {
        MemberRepository repository = mock(MemberRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(encoder.encode("secret-password")).thenReturn("encoded");
        when(repository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AdminBootstrap bootstrap = new AdminBootstrap(repository, encoder);
        ReflectionTestUtils.setField(bootstrap, "loginId", "admin");
        ReflectionTestUtils.setField(bootstrap, "password", "secret-password");

        bootstrap.run(new DefaultApplicationArguments(new String[0]));

        verify(repository).save(argThat(member -> member.getLoginId().equals("admin")
                && member.getPasswordHash().equals("encoded") && member.getRole() == Role.ADMIN));
    }
}
