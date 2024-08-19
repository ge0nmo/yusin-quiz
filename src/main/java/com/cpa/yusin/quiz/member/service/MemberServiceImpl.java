package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.controller.port.MemberService;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class MemberServiceImpl implements MemberService
{
    private final MemberRepository memberRepository;

    @Override
    public MemberDTO getById(long id)
    {
        log.info("member id = {}", id);

        return MemberDTO.from(findById(id));
    }

    @Override
    public MemberDomain findById(long id)
    {
        return memberRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }


}
