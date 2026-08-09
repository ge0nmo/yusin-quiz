package com.cpa.yusin.quiz.problem.domain;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.common.domain.ContentStatus;
import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.converter.JsonBlockListConverter;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamSubject;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "problem", uniqueConstraints =
        @UniqueConstraint(name = "uk_problem_exam_subject_number",
                columnNames = {"exam_id", "qualification_exam_subject_id", "number"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qualification_exam_subject_id", nullable = false)
    private QualificationExamSubject subjectMapping;

    @Column(nullable = false)
    private int number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status;

    @Convert(converter = JsonBlockListConverter.class)
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private List<Map<String, Object>> content = new ArrayList<>();

    @Convert(converter = JsonBlockListConverter.class)
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private List<Map<String, Object>> explanation = new ArrayList<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("number ASC")
    private List<Choice> choices = new ArrayList<>();

    public Problem(Exam exam, QualificationExamSubject subjectMapping, int number,
                   ContentStatus status, List<Map<String, Object>> content,
                   List<Map<String, Object>> explanation) {
        update(exam, subjectMapping, number, status, content, explanation);
    }

    public void update(Exam exam, QualificationExamSubject subjectMapping, int number,
                       ContentStatus status, List<Map<String, Object>> content,
                       List<Map<String, Object>> explanation) {
        this.exam = exam;
        this.subjectMapping = subjectMapping;
        this.number = number;
        this.status = status;
        this.content = new ArrayList<>(content == null ? List.of() : content);
        this.explanation = new ArrayList<>(explanation == null ? List.of() : explanation);
    }

    public void replaceChoices(List<Choice> newChoices) {
        choices.clear();
        newChoices.forEach(choice -> {
            choice.attachTo(this);
            choices.add(choice);
        });
    }
}
