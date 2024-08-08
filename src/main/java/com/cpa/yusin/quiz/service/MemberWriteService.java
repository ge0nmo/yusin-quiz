package com.cpa.yusin.quiz.service;

import com.cpa.yusin.quiz.domain.entity.Member;
import com.cpa.yusin.quiz.domain.entity.type.Platform;

public interface MemberWriteService
{
    Member register(String email, String password, String username, Platform platform);
}
