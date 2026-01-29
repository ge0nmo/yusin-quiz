package com.cpa.yusin.quiz.problem.domain;

import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.converter.BlockListConverter;
import com.cpa.yusin.quiz.problem.domain.block.Block;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // [V1 Legacy] HTML 기반 데이터 (삭제 예정 아님, 호환성 유지)
    // =========================================================
    @Column(columnDefinition = "LONGTEXT") // nullable 허용 (V2로 생성시 null일 수 있음)
    private String content;

    @Column(columnDefinition = "LONGTEXT")
    private String explanation;

    // =========================================================
    // [V2 New] JSON Block 기반 데이터
    // =========================================================
    @Column(columnDefinition = "json")
    @Convert(converter = BlockListConverter.class)
    private List<Block> contentJson;

    @Column(columnDefinition = "json")
    @Convert(converter = BlockListConverter.class)
    private List<Block> explanationJson;


    // 공통 필드
    @Column(nullable = false)
    private int number;

    private boolean isRemoved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    // =========================================================
    // [V1 Methods] 기존 HTML 방식 유지
    // =========================================================
    // 생성용 (Factory)
    public static Problem fromSaveOrUpdate(String content, String explanation, int number, Exam exam) {
        return Problem.builder()
                .content(content)
                .explanation(explanation)
                .number(number)
                .exam(exam)
                .isRemoved(false)
                .build();
    }

    // 수정용 (Dirty Checking)
    public void update(String content, int number, String explanation) {
        this.content = content;
        this.number = number;
        this.explanation = explanation;
    }

    // =========================================================
    // [V2 Methods] JSON 방식 추가 (오버로딩)
    // =========================================================

    /**
     * [V2 생성] JSON Block 리스트를 받아 Problem 객체를 생성합니다.
     * Service에서 request.isNew()일 때 호출됩니다.
     */
// [V2 New] JSON Block 리스트를 받아 생성하는 팩토리 메서드
    public static Problem fromSaveOrUpdate(List<Block> contentJson, List<Block> explanationJson, int number, Exam exam) {
        return Problem.builder()
                .contentJson(contentJson)         // JSON 데이터
                .explanationJson(explanationJson) // JSON 데이터
                .number(number)
                .exam(exam)
                .content("")      // 레거시 필드는 빈값 처리
                .explanation("")  // 레거시 필드는 빈값 처리
                .isRemoved(false)
                .build();
    }

    /**
     * [V2 수정] 기존 객체의 JSON Block 데이터를 변경합니다.
     * Service에서 !request.isNew()일 때 호출됩니다.
     */
    public void update(List<Block> contentJson, int number, List<Block> explanationJson) {
        this.contentJson = contentJson;
        this.number = number;
        this.explanationJson = explanationJson;

        // V2로 수정되었음을 표시하기 위해 V1 필드를 비우거나,
        // 필요하다면 역변환(JSON->HTML)해서 채워넣을 수도 있습니다. (여기선 비우는 걸 추천)
        this.content = "";
        this.explanation = "";
    }

    // =========================================================
    // 공통 메서드
    // =========================================================
    public void delete() {
        this.isRemoved = true;
    }

    // 마이그레이션용 (HTML -> JSON 변환 시 사용)
    public void migrateToBlocks(List<Block> contentJson, List<Block> explanationJson) {
        this.contentJson = contentJson;
        this.explanationJson = explanationJson;
    }
}