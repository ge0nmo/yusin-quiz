package com.cpa.yusin.quiz.member.infrastructure;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseEntity
{
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

    private LocalDateTime subscriberExpiredAt;

    public static Member fromDomain(MemberDomain memberDomain)
    {
        Member member = new Member();
        member.id = memberDomain.getId();
        member.email = memberDomain.getEmail();
        member.password = memberDomain.getPassword();
        member.username = memberDomain.getUsername();
        member.platform = memberDomain.getPlatform();
        member.role = memberDomain.getRole();

        return member;
    }

    public MemberDomain toDomain()
    {
        return MemberDomain.builder()
                .id(id)
                .email(email)
                .password(password)
                .username(username)
                .platform(platform)
                .role(role)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }
}
