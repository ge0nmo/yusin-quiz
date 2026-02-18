package com.cpa.yusin.quiz.study.event;

public record StudySolvedEvent(Long memberId, int solvedCount) {
}
