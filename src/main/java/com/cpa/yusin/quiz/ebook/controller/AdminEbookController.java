package com.cpa.yusin.quiz.ebook.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.ebook.service.*;
import com.cpa.yusin.quiz.global.exception.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** 기존 /api/admin/** 보안 경계를 재사용하며 파일만 바이너리 응답으로 내려줍니다. */
@RestController
@RequestMapping("/api/admin/ebooks")
@RequiredArgsConstructor
public class AdminEbookController {
    private final EbookSnapshotService snapshots;
    private final EbookGenerationService generation;

    @GetMapping("/catalog")
    public GlobalResponse<List<EbookDto.CatalogRow>> catalog() { return GlobalResponse.success(snapshots.catalog()); }

    @PostMapping(value = "/epub")
    public ResponseEntity<byte[]> generate(@Valid @RequestBody EbookDto.GenerateRequest request) {
        byte[] bytes = generation.generate(request);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/epub+zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(request.qualificationCode().name().toLowerCase(java.util.Locale.ROOT) + ".epub").build().toString())
                .cacheControl(CacheControl.noStore()).contentLength(bytes.length).body(bytes);
    }

    @ExceptionHandler(EbookValidationException.class)
    public ResponseEntity<ErrorResponse> invalid(EbookValidationException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage(), "EBOOK_INVALID_CONTENT"));
    }
    @ExceptionHandler(EbookGenerationService.EbookBusyException.class)
    public ResponseEntity<ErrorResponse> busy() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header(HttpHeaders.RETRY_AFTER, "10")
                .body(ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS, "다른 전자책을 생성 중입니다. 잠시 후 다시 시도해주세요.", "EBOOK_BUSY"));
    }
}
