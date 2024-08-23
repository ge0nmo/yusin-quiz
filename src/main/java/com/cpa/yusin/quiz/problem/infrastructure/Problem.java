package com.cpa.yusin.quiz.problem.infrastructure;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.exam.infrastructure.Exam;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exam_id", "number"})
})
@Getter
@Entity
public class Problem extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    public static Problem from(ProblemDomain domain)
    {
        Problem problem = new Problem();
        problem.id = domain.getId();
        problem.content = domain.getContent();
        problem.number = domain.getNumber();
        problem.exam = Exam.from(domain.getExam());
        return problem;
    }

    public ProblemDomain toModel()
    {
        return ProblemDomain.builder()
                .id(this.id)
                .content(this.content)
                .exam(this.exam.toModel())
                .build();
    }
}
