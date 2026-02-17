package com.cpa.yusin.quiz.study.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "study_session")
public class StudySession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Long examId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudySessionStatus status;

    @Column(nullable = false)
    private int lastIndex; // 마지막으로 푼(접근한) 문제 인덱스

    private Integer currentScore; // EXAM 모드 완료 시 점수

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public void updateLastIndex(int index) {
        this.lastIndex = index;
    }

    public void complete(int score) {
        this.status = StudySessionStatus.COMPLETED;
        this.currentScore = score;
        this.finishedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = StudySessionStatus.COMPLETED;
        this.finishedAt = LocalDateTime.now();
    }

    public static StudySession start(Member member, Long examId, ExamMode mode) {
        return StudySession.builder()
                .member(member)
                .examId(examId)
                .mode(mode)
                .status(StudySessionStatus.IN_PROGRESS)
                .lastIndex(0)
                .startedAt(LocalDateTime.now())
                .build();
    }
}
