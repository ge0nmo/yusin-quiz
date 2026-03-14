package com.cpa.yusin.quiz.question.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.problem.domain.Problem;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Getter
@Entity
public class Question extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    private String content;

    @Column(nullable = false)
    private boolean answeredByAdmin;

    @Column(nullable = false)
    private Integer answerCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    private boolean isRemoved;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void answerByAdmin() {
        this.answeredByAdmin = true;
        updateAnswerCount(1);
    }

    public void updateAnswerCount(int count) {
        int currentCount = this.answerCount == null ? 0 : this.answerCount;
        int newCount = currentCount + count;
        this.answerCount = Math.max(newCount, 0);
    }

    public void delete() {
        this.isRemoved = true;
    }

    public void syncAnsweredByAdmin(boolean answeredByAdmin) {
        this.answeredByAdmin = answeredByAdmin;
    }

    /**
     * 작성자 본인 또는 관리자인지 확인
     */
    public boolean isOwner(Member requestMember) {
        if (requestMember == null)
            return false;
        return this.member.getId().equals(requestMember.getId())
                || Role.ADMIN.equals(requestMember.getRole());
    }
}
