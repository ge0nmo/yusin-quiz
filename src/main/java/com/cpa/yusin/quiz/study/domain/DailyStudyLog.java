package com.cpa.yusin.quiz.study.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "daily_study_log", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_study_log_member_date", columnNames = { "member_id", "date" })
})
public class DailyStudyLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int solvedCount;

    public void increaseSolvedCount() {
        this.solvedCount++;
    }

    public static DailyStudyLog create(Member member, LocalDate date) {
        return DailyStudyLog.builder()
                .member(member)
                .date(date)
                .solvedCount(0)
                .build();
    }

    public static DailyStudyLog createFirst(Member member, LocalDate date) {
        return DailyStudyLog.builder()
                .member(member)
                .date(date)
                .solvedCount(1)
                .build();
    }

    public static DailyStudyLog createWithCount(Member member, LocalDate date, int count) {
        return DailyStudyLog.builder()
                .member(member)
                .date(date)
                .solvedCount(count)
                .build();
    }
}
