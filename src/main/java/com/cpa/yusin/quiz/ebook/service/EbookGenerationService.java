package com.cpa.yusin.quiz.ebook.service;

import com.cpa.yusin.quiz.ebook.controller.EbookDto.GenerateRequest;
import com.cpa.yusin.quiz.ebook.model.BookComposer;
import com.cpa.yusin.quiz.ebook.render.EpubRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class EbookGenerationService {
    private final EbookSnapshotService snapshots;
    private final BookComposer composer;
    private final EpubRenderer renderer;
    // 수동 제작용 초기 버전은 서버별 한 건씩 처리합니다. 대기열/파일 영구 보관 없이 메모리 경쟁을 제한합니다.
    private final Semaphore slot = new Semaphore(1);

    public byte[] generate(GenerateRequest request) {
        if (!slot.tryAcquire()) throw new EbookBusyException();
        try { return renderer.render(composer.compose(snapshots.capture(request), request.settings()), request.settings()); }
        finally { slot.release(); }
    }

    public static class EbookBusyException extends RuntimeException {}
}
