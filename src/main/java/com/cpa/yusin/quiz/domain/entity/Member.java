package com.cpa.yusin.quiz.domain.entity;

import com.cpa.yusin.quiz.domain.entity.type.Platform;
import com.cpa.yusin.quiz.domain.entity.type.Role;
import com.cpa.yusin.quiz.domain.entity.type.SubscribeStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String username;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SubscribeStatus subscribeStatus;

    @Builder
    public Member(String email, String password, String username, Platform platform)
    {
        this.email = email;
        this.password = password;
        this.username = username;
        this.platform = platform;
        this.role = Role.USER;
        this.subscribeStatus = SubscribeStatus.DEFAULT;
    }
}
