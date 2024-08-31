package com.cpa.yusin.quiz.member.controller.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberUpdateRequest
{
    private String username;
}
