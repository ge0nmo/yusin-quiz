package com.cpa.yusin.quiz.study.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "submitted_answer", uniqueConstraints = {
        @UniqueConstraint(name = "uk_submitted_answer_session_problem", columnNames = { "study_session_id",
                "problem_id" })
})
public class SubmittedAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_session_id", nullable = false)
    private StudySession studySession;

    @Column(nullable = false)
    private Long problemId;

    @Column(nullable = false)
    private Long choiceId;

    @Column(nullable = false)
    private boolean isCorrect;

    public void updateAnswer(Long choiceId, boolean isCorrect) {
        this.choiceId = choiceId;
        this.isCorrect = isCorrect;
    }

    public static SubmittedAnswer create(StudySession session, Long problemId, Long choiceId, boolean isCorrect) {
        return SubmittedAnswer.builder()
                .studySession(session)
                .problemId(problemId)
                .choiceId(choiceId)
                .isCorrect(isCorrect)
                .build();
    }
}
