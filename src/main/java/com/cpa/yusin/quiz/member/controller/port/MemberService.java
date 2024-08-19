package com.cpa.yusin.quiz.member.controller.port;

import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.domain.MemberDomain;

public interface MemberService
{
    MemberDTO getById(long id);

    MemberDomain findById(long id);
}
