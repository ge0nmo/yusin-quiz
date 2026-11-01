package com.cpa.yusin.quiz.content.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.content.controller.dto.PublicContentV2Dto.*;
import com.cpa.yusin.quiz.content.service.PublicContentV2Service;
import com.cpa.yusin.quiz.content.service.PublicContentV2Service.Versioned;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/qualification-exams/{code}")
@RequiredArgsConstructor
public class PublicContentV2Controller {
    private final PublicContentV2Service contentService;

    @GetMapping("/content")
    public ResponseEntity<GlobalResponse<ContentResponse>> getContent(
            @PathVariable String code,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        return toResponse(contentService.getContent(code, ifNoneMatch));
    }

    /**
     * 해설은 향후 이용권 확인을 붙일 엔드포인트다. 지금은 로그인 없이 열려 있다.
     */
    @GetMapping("/explanations")
    public ResponseEntity<GlobalResponse<ExplanationsResponse>> getExplanations(
            @PathVariable String code,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        return toResponse(contentService.getExplanations(code, ifNoneMatch));
    }

    private <T> ResponseEntity<GlobalResponse<T>> toResponse(Versioned<T> versioned) {
        ResponseEntity.BodyBuilder builder = ResponseEntity
                .status(versioned.notModified() ? HttpStatus.NOT_MODIFIED : HttpStatus.OK)
                .eTag(versioned.version())
                .cacheControl(CacheControl.noCache());
        return versioned.notModified() ? builder.build() : builder.body(GlobalResponse.success(versioned.body()));
    }
}
