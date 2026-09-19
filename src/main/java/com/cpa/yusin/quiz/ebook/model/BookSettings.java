package com.cpa.yusin.quiz.ebook.model;

/** 정답/해설 위치를 분리하여 템플릿 수정 없이 생성 옵션만으로 조판을 바꿉니다. */
public record BookSettings(YearOrder yearOrder, Placement answerPlacement, Placement explanationPlacement) {
    public enum YearOrder { NEWEST_FIRST, OLDEST_FIRST }
    public enum Placement { AFTER_QUESTION, YEAR_END, BOOK_END }
}
