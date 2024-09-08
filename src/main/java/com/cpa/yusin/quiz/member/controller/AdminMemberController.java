package com.cpa.yusin.quiz.member.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.controller.port.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/members")
@RestController
public class AdminMemberController
{
    private final MemberService memberService;

    @PatchMapping("/{id}")
    public ResponseEntity<GlobalResponse<MemberDTO>> update(@Positive @PathVariable("id") long id,
                                                            @RequestBody @Valid MemberUpdateRequest request,
                                                            @AuthenticationPrincipal MemberDetails memberDetails)
    {
        memberService.update(id, request, memberDetails.getMemberDomain());
        MemberDTO response = memberService.getById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GlobalResponse<>(response));
    }


    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<MemberDTO>> getById(@PathVariable("id") Long id)
    {
        MemberDTO response = memberService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<MemberDTO>>> getMembers(@RequestParam(required = false) String keyword,
                                                                      @PageableDefault Pageable pageable)
    {
        Page<MemberDTO> response = memberService.getAll(keyword, pageable.previousOrFirst());

        return ResponseEntity.ok(new GlobalResponse<>(response.getContent(), PageInfo.of(response)));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<Void>> deleteById(@Positive @PathVariable("id") long id,
                                                           @AuthenticationPrincipal MemberDetails memberDetails)
    {
        memberService.deleteById(id, memberDetails.getMemberDomain());

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(new GlobalResponse<>());
    }
}
