package com.cpa.yusin.quiz.choice.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.global.converter.JsonBlockListConverter;
import com.cpa.yusin.quiz.problem.domain.Problem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "problem_choice", uniqueConstraints =
        @UniqueConstraint(name = "uk_choice_problem_number", columnNames = {"problem_id", "number"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Choice extends BaseEntity {
    public static final int MAX_IMAGE_URL_LENGTH = 2048;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean answer;

    /** 보기 이미지 URL. 문제 본문 이미지 블록의 src처럼 관리자가 보낸 URL을 그대로 두고, 읽을 때 관리 이미지를 다시 서명한다. */
    @Column(name = "image_url", length = MAX_IMAGE_URL_LENGTH)
    private String imageUrl;

    @Convert(converter = JsonBlockListConverter.class)
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private List<Map<String, Object>> explanation = new ArrayList<>();

    public Choice(int number, String content, boolean answer, List<Map<String, Object>> explanation) {
        this(number, content, null, answer, explanation);
    }

    public Choice(int number, String content, String imageUrl, boolean answer,
                  List<Map<String, Object>> explanation) {
        this.number = number;
        update(content, imageUrl, answer, explanation);
    }

    public void attachTo(Problem problem) {
        this.problem = problem;
    }

    public void update(String content, boolean answer, List<Map<String, Object>> explanation) {
        update(content, imageUrl, answer, explanation);
    }

    public void update(String content, String imageUrl, boolean answer, List<Map<String, Object>> explanation) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.answer = answer;
        this.explanation = new ArrayList<>(explanation == null ? List.of() : explanation);
    }
}
