package com.cpa.yusin.quiz.ebook.controller;

import com.cpa.yusin.quiz.ebook.model.BookSettings;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import jakarta.validation.constraints.*;
import java.util.List;

public final class EbookDto {
    private EbookDto() {}
    /** 빈 선택을 '전체'로 추측하지 않습니다. 화면이 전체 항목을 명시적으로 전송합니다. */
    public record GenerateRequest(@NotNull QualificationExamCode qualificationCode,
                                  @NotEmpty @Size(max = 200) List<@NotNull @Positive Long> subjectIds,
                                  @NotEmpty @Size(max = 200) List<@NotNull @Min(1900) @Max(3000) Integer> years,
                                  @NotBlank @Size(max = 160) String title,
                                  @NotNull BookSettings.YearOrder yearOrder,
                                  @NotNull BookSettings.Placement answerPlacement,
                                  @NotNull BookSettings.Placement explanationPlacement) {
        public BookSettings settings() { return new BookSettings(yearOrder, answerPlacement, explanationPlacement); }
    }
    public record CatalogRow(QualificationExamCode qualificationCode, String qualificationName,
                             long subjectId, String subjectName, int year, long questionCount) {}
}
