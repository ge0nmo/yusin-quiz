package com.cpa.yusin.quiz.member.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import jakarta.persistence.*;
import lombok.*;

@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    public static Member fromHome(String email, String encodedPassword, String username) {
        return Member.builder()
                .email(email)
                .password(encodedPassword)
                .username(username)
                .platform(Platform.HOME)
                .role(Role.USER)
                .build();
    }

    public void updateFromOauth2(String newUsername) {
        this.username = newUsername;
    }

    public void update(String username) {
        this.username = username;
    }

    public void validateMember(long memberId, Member member) {
        if (member.getId().equals(memberId) || Role.ADMIN.equals(member.getRole())) {
            return;
        }

        throw new MemberException(ExceptionMessage.NO_AUTHORIZATION);
    }

    public void changeRole(Role role) {
        this.role = role;
    }
}
