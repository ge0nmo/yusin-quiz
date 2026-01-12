package com.cpa.yusin.quiz.member.service.dto;

import com.cpa.yusin.quiz.member.domain.type.Platform;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialProfile
{
    private String email;
    private String name;
    private Platform platform;
}