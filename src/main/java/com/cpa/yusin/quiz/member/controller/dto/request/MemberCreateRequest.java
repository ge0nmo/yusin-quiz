package com.cpa.yusin.quiz.member.controller.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberCreateRequest
{
    private String email;
    private String password;
    private String username;

}
