package com.cpa.yusin.quiz.member.controller.dto.request;

import lombok.Getter;

@Getter
public class MemberCreateRequest
{
    private String email;
    private String password;
    private String username;

}
