package com.cpa.yusin.quiz.problem.domain;

import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.converter.BlockListConverter;
import com.cpa.yusin.quiz.problem.domain.block.Block;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    // [V1 Legacy] HTML 기반 데이터 (호환성 유지)
    // =========================================================
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(columnDefinition = "LONGTEXT")
    private String explanation;

    // =========================================================
    // [V2 New] JSON Block 기반 데이터
    // =========================================================
    // BlockListConverter가 List<Block> <-> JSON String 변환을 담당합니다.
    @Column(columnDefinition = "json")
    @Convert(converter = BlockListConverter.class)
    private List<Block> contentJson;

    @Column(columnDefinition = "json")
    @Convert(converter = BlockListConverter.class)
    private List<Block> explanationJson;

    // 공통 필드
    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    private boolean isRemoved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    // =========================================================
    // [V1 Methods] Legacy Factory
    // =========================================================
    public static Problem fromSaveOrUpdate(String content, String explanation, int number, Exam exam) {
        return Problem.builder()
                .content(content)
                .explanation(explanation)
                .number(number)
                .exam(exam)
                .contentJson(new ArrayList<>())
                .explanationJson(new ArrayList<>())
                .isRemoved(false)
                .build();
    }

    public void update(String content, int number, String explanation) {
        this.content = content;
        this.number = number;
        this.explanation = explanation;
    }

    // =========================================================
    // [V2 Methods] JSON Block Factory & Update
    // =========================================================

    /**
     * [V2 생성] JSON Block 리스트를 받아 Problem 객체 생성
     * Client에서 이미지가 URL로 넘어오므로, 그대로 저장하면 됩니다.
     */
    public static Problem fromSaveOrUpdate(List<Block> contentJson, List<Block> explanationJson, int number, Exam exam) {
        return Problem.builder()
                .contentJson(contentJson)
                .explanationJson(explanationJson)
                .number(number)
                .exam(exam)
                .isRemoved(false)
                .build();
    }

    /**
     * [V2 수정] JSON Block 데이터 업데이트
     */
    public void update(List<Block> contentJson, int number, List<Block> explanationJson) {
        this.contentJson = contentJson;
        this.number = number;
        this.explanationJson = explanationJson;
    }

    // =========================================================
    // 공통 메서드
    // =========================================================
    public void delete() {
        this.isRemoved = true;
    }

    // 마이그레이션 유틸리티
    public void migrateToBlocks(List<Block> contentJson, List<Block> explanationJson) {
        this.contentJson = contentJson;
        this.explanationJson = explanationJson;
    }
}