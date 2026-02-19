package com.cpa.yusin.quiz.bookmark.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.problem.domain.Problem;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bookmark", uniqueConstraints = @UniqueConstraint(name = "uk_bookmark_member_problem", columnNames = {
        "member_id", "problem_id" }))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    /**
     * 정적 팩토리 메서드: Bookmark 생성
     */
    public static Bookmark create(Member member, Problem problem) {
        return Bookmark.builder()
                .member(member)
                .problem(problem)
                .build();
    }
}
