package com.cpa.yusin.quiz.member.controller.mapper;

import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
import org.springframework.stereotype.Component;

@Component
public class MemberMapperImpl implements MemberMapper
{
    @Override
    public MemberCreateResponse toMemberCreateResponse(MemberDomain memberDomain)
    {
        if(memberDomain == null)
            return null;

        return MemberCreateResponse.builder()
                .id(memberDomain.getId())
                .email(memberDomain.getEmail())
                .username(memberDomain.getUsername())
                .platform(memberDomain.getPlatform())
                .role(memberDomain.getRole())
                .build();
    }

    @Override
    public MemberDTO toMemberDTO(MemberDomain memberDomain)
    {
        if(memberDomain == null)
            return null;

        return MemberDTO.builder()
                .id(memberDomain.getId())
                .email(memberDomain.getEmail())
                .username(memberDomain.getUsername())
                .platform(memberDomain.getPlatform())
                .role(memberDomain.getRole())
                .createdAt(memberDomain.getCreatedAt())
                .updatedAt(memberDomain.getUpdatedAt())
                .build();
    }
}
