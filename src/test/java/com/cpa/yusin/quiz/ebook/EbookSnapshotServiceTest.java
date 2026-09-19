package com.cpa.yusin.quiz.ebook;

import com.cpa.yusin.quiz.common.service.*;
import com.cpa.yusin.quiz.ebook.controller.EbookDto.GenerateRequest;
import com.cpa.yusin.quiz.ebook.infrastructure.EbookRepository;
import com.cpa.yusin.quiz.ebook.model.BookSettings.*;
import com.cpa.yusin.quiz.ebook.service.*;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EbookSnapshotServiceTest {
    @Test
    void rejectsOversizedAndEmptyScopesBeforeLoadingBodies() {
        var repository = mock(EbookRepository.class);
        var service = new EbookSnapshotService(repository, new BlockNormalizer(), mock(ClockHolder.class),
                mock(UuidHolder.class), new ObjectMapper());
        var request = new GenerateRequest(QualificationExamCode.APPRAISER, List.of(1L), List.of(2025), "복습",
                YearOrder.NEWEST_FIRST, Placement.AFTER_QUESTION, Placement.YEAR_END);
        when(repository.countSelected(request.qualificationCode(), request.subjectIds(), request.years())).thenReturn(5001L);
        assertThatThrownBy(() -> service.capture(request)).isInstanceOf(EbookValidationException.class).hasMessageContaining("5,000");
        when(repository.countSelected(request.qualificationCode(), request.subjectIds(), request.years())).thenReturn(0L);
        assertThatThrownBy(() -> service.capture(request)).isInstanceOf(EbookValidationException.class).hasMessageContaining("없습니다");
        verify(repository, never()).snapshot(any(), anyList(), anyList());
    }
}
