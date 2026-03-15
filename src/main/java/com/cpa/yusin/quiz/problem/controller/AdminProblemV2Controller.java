package com.cpa.yusin.quiz.problem.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemSaveV2Request;
import com.cpa.yusin.quiz.problem.controller.dto.response.AdminProblemSearchResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import com.cpa.yusin.quiz.problem.controller.port.CreateProblemV2Service;
import com.cpa.yusin.quiz.problem.controller.port.GetProblemV2Service;
import com.cpa.yusin.quiz.problem.controller.port.SearchAdminProblemV2Service;
import com.cpa.yusin.quiz.problem.service.dto.AdminProblemLectureStatus;
import com.cpa.yusin.quiz.problem.service.dto.AdminProblemSearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/admin/problem")
@RestController
public class AdminProblemV2Controller
{
    private final CreateProblemV2Service createProblemV2Service;
    private final GetProblemV2Service getProblemV2Service;
    private final SearchAdminProblemV2Service searchAdminProblemV2Service;

    /**
     * 관리자 문제 편집 화면이 사용하는 생성/수정 통합 API.
     *
     * 왜 단일 엔드포인트로 유지하는가:
     * - Next.js 관리자 화면은 문제 추가/수정 UI를 동일 폼으로 사용 중
     * - 프론트는 id 유무만 보고 같은 요청 shape를 재사용하는 편이 구현 비용이 가장 낮음
     *
     * lecture 필드 규칙:
     * - lecture = null 이면 해설강의 링크를 제거해야 함
     * - lecture.youtubeUrl 만 있으면 "해설 링크만 노출" 상태로 저장해야 함
     * - lecture.startTimeSecond 는 nullable 이며, 값이 있으면 프론트가 playbackUrl 또는 `&t=`를 사용해
     *   특정 시점부터 재생 가능
     *
     * 프론트 구현 포인트:
     * - 관리 화면은 저장 전에 유튜브 URL 원본을 그대로 보낼 수 있음
     * - 서버가 canonical watch URL로 정규화해서 저장하므로 프론트는 별도 URL 정규화 로직을 가질 필요 없음
     * - 잘못된 유튜브 도메인, video id 추출 실패, 음수 시작 시간은 400 Bad Request 로 처리됨
     */
    @PostMapping
    public ResponseEntity<GlobalResponse<String>> saveOrUpdateV2(@RequestParam("examId") long examId,
                                                                 @Validated @RequestBody ProblemSaveV2Request request
    ) {
        createProblemV2Service.saveOrUpdateV2(examId, request);
        return ResponseEntity.ok(new GlobalResponse<>("Saved Successfully (V2)"));
    }

    /**
     * 관리자 문제 상세 조회 API.
     *
     * 프론트는 문제 편집 화면 최초 진입 시 이 응답 하나만으로
     * - 본문/해설 block 데이터
     * - 보기 목록
     * - 해설강의 lecture 정보
     * 를 모두 채울 수 있어야 함.
     */
    @GetMapping("/{problemId}")
    public ResponseEntity<GlobalResponse<ProblemV2Response>> getById(@PathVariable Long problemId)
    {
        ProblemV2Response response = getProblemV2Service.getByIdForAdmin(problemId);
        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    /**
     * 시험별 문제 목록 조회 API.
     *
     * 관리자 문제 목록 화면은 각 문제 row에서 해설강의 존재 여부를 즉시 보여줘야 하므로
     * lecture 객체를 함께 내려야 함.
     * lecture 가 null 이면 아직 해설강의 링크가 연결되지 않은 문제라는 의미.
     */
    @GetMapping
    public ResponseEntity<GlobalResponse<List<ProblemV2Response>>> getAllByExamId(@RequestParam Long examId)
    {
        List<ProblemV2Response> response = getProblemV2Service.getAllByExamIdForAdmin(examId);
        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    /**
     * 대시보드 카드 클릭 진입용 관리자 문제 검색 API.
     *
     * 시험 단위 목록과 분리한 이유:
     * - 대시보드의 "강의 미연결 문제"는 전역 집계이므로 examId 없이도 바로 조회 가능해야 함
     * - subject/year/exam 보조 필터를 함께 받아도 active hierarchy 기준은 항상 동일해야 함
     */
    @GetMapping("/search")
    public ResponseEntity<GlobalResponse<List<AdminProblemSearchResponse>>> search(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(defaultValue = "ALL") AdminProblemLectureStatus lectureStatus,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long examId) {
        Page<AdminProblemSearchResponse> response = searchAdminProblemV2Service.search(
                pageable,
                AdminProblemSearchCondition.of(lectureStatus, subjectId, year, examId)
        );

        return ResponseEntity.ok(new GlobalResponse<>(response.getContent(), PageInfo.of(response)));
    }
}
