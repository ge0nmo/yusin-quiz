package com.cpa.yusin.quiz.ebook.render;

import com.cpa.yusin.quiz.ebook.model.BookComposer.Composition;
import com.cpa.yusin.quiz.ebook.model.BookSettings;

/** 향후 PDF는 이 경계에서 추가합니다. 데이터 수집이나 배치 규칙을 복제하지 않습니다. */
public interface BookRenderer {
    byte[] render(Composition composition, BookSettings settings);
}
