package com.cpa.yusin.quiz.member.controller.mapper;

import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.domain.MemberDomain;

public interface MemberMapper
{
    MemberCreateResponse toMemberCreateResponse(MemberDomain memberDomain);

    MemberDTO toMemberDTO(MemberDomain memberDomain);
}
