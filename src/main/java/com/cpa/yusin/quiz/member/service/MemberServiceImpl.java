package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.controller.mapper.MemberMapper;
import com.cpa.yusin.quiz.member.controller.port.MemberService;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class MemberServiceImpl implements MemberService
{
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;


    @Override
    public void update(long memberId, MemberUpdateRequest request, MemberDomain loggedInMember)
    {
        loggedInMember.validateMember(memberId, loggedInMember);

        MemberDomain targetMember = findById(memberId);
        targetMember = targetMember.update(request);

        memberRepository.save(targetMember);
    }

    @Override
    public MemberDTO getById(long id)
    {
        log.info("member id = {}", id);

        return memberMapper.toMemberDTO(findById(id));
    }

    @Override
    public Page<MemberDTO> getAll(String keyword, Pageable pageable)
    {
        if(StringUtils.hasLength(keyword))
            keyword = keyword.toLowerCase();

        return memberRepository.findAllByKeyword(keyword, pageable)
                .map(memberMapper::toMemberDTO);
    }

    @Override
    public MemberDomain findById(long id)
    {
        return memberRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }

    @Override
    public void deleteById(long id, MemberDomain loggedInMember)
    {
        loggedInMember.validateMember(id, loggedInMember);
        memberRepository.deleteById(id);
    }

}
