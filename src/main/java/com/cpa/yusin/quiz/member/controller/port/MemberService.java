package com.cpa.yusin.quiz.member.controller.port;

import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.infrastructure.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService
{
    void update(long memberId, MemberUpdateRequest request, MemberDomain memberDomain);

    MemberDTO getById(long id);

    Page<MemberDTO> getAll(String keyword, Pageable pageable);

    MemberDomain findById(long id);

    void deleteById(long id, MemberDomain loggedInMember);
}
