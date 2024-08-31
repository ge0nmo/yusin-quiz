package com.cpa.yusin.quiz.member.controller.dto.response;

import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class MemberDTO
{
    private final long id;
    private final String email;
    private final String username;
    private final Platform platform;
    private final Role role;
    private LocalDateTime subscriberExpiredAt;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
