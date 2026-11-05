package com.cpa.yusin.quiz.report.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.qualification.domain.QualificationExam;
import com.cpa.yusin.quiz.report.infrastructure.ChoiceNumbersConverter;
import com.cpa.yusin.quiz.version.domain.AppPlatform;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 앱 사용자가 보낸 문제 오류 신고 또는 건의·문의. 회원이 없으므로 신고자는 익명 기기 ID로만 구분한다.
 * <p>
 * 문제가 삭제되면 {@code problem}만 끊고 작성 시점의 문제 스냅숏(번호, 회차, 연도, 과목)은 남긴다.
 * 서비스가 삭제 전에 먼저 끊고, 외래 키도 {@code ON DELETE SET NULL}로 만들어 다른 삭제 경로에서도 막히지 않게 한다.
 * </p>
 */
@Entity
@Table(name = "report", indexes = {
        @Index(name = "idx_report_device_created", columnList = "device_id, created_at"),
        @Index(name = "idx_report_problem_status", columnList = "problem_id, status"),
        @Index(name = "idx_report_status_created", columnList = "status, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {
    public static final int MAX_CONTENT_LENGTH = 1000;
    public static final int MAX_ANSWER_LENGTH = 1000;
    public static final int MAX_APP_VERSION_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qualification_exam_id", nullable = false)
    private QualificationExam qualificationExam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Problem problem;

    @Column(name = "problem_number")
    private Integer problemNumber;

    @Column(name = "exam_name", length = 120)
    private String examName;

    @Column(name = "exam_year")
    private Integer examYear;

    @Column(name = "subject_name", length = 100)
    private String subjectName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    @Convert(converter = ChoiceNumbersConverter.class)
    @Column(name = "selected_choice_numbers", nullable = false, length = 20)
    private List<Integer> selectedChoiceNumbers = new ArrayList<>();

    @Column(name = "device_id", nullable = false, length = 36)
    private String deviceId;

    @Column(name = "app_version", nullable = false, length = MAX_APP_VERSION_LENGTH)
    private String appVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppPlatform platform;

    @Column(length = MAX_ANSWER_LENGTH)
    private String answer;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    public Report(QualificationExam qualificationExam, Problem problem, ReportCategory category, String content,
                  List<Integer> selectedChoiceNumbers, String deviceId, String appVersion, AppPlatform platform) {
        this.qualificationExam = qualificationExam;
        this.problem = problem;
        if (problem != null) {
            this.problemNumber = problem.getNumber();
            this.examName = problem.getExam().getName();
            this.examYear = problem.getExam().getYear();
            this.subjectName = problem.getSubjectMapping().getSubject().getName();
        }
        this.category = category;
        this.status = ReportStatus.RECEIVED;
        this.content = content;
        this.selectedChoiceNumbers = new ArrayList<>(selectedChoiceNumbers);
        this.deviceId = deviceId;
        this.appVersion = appVersion;
        this.platform = platform;
    }

    /** 문제가 한 번이라도 연결된 신고인지. 문제가 삭제돼도 스냅숏이 남아 있으면 true다. */
    public boolean hasProblemSnapshot() {
        return problemNumber != null;
    }

    /**
     * 관리자 처리. 종료 상태로 저장할 때마다 답변 시각을 지금으로 바꾸고, 미처리로 되돌리면 답변 시각만 지운다.
     */
    public void process(ReportStatus status, String answer, LocalDateTime now) {
        this.status = status;
        this.answer = answer;
        this.answeredAt = status.isClosed() ? now : null;
    }
}
