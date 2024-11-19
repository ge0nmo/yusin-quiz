package com.cpa.yusin.quiz.member.controller.port;

import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService
{
    void update(long memberId, MemberUpdateRequest request, Member member);

    MemberDTO getById(long id);

    Page<MemberDTO> getAll(String keyword, Pageable pageable);

    Page<Member> getAllAdminNot(String keyword, Pageable pageable);

    Member findById(long id);

    void deleteById(long id, Member loggedInMember);
}
