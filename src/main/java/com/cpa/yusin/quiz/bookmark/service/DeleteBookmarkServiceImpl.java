package com.cpa.yusin.quiz.bookmark.service;

import com.cpa.yusin.quiz.bookmark.controller.port.DeleteBookmarkService;
import com.cpa.yusin.quiz.bookmark.service.port.BookmarkRepository;
import com.cpa.yusin.quiz.global.exception.BookmarkException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteBookmarkServiceImpl implements DeleteBookmarkService {

    private final BookmarkRepository bookmarkRepository;

    @Override
    public void delete(Long memberId, Long problemId) {
        // 존재 여부 확인 후 삭제
        if (!bookmarkRepository.existsByMemberIdAndProblemId(memberId, problemId)) {
            throw new BookmarkException(ExceptionMessage.BOOKMARK_NOT_FOUND);
        }

        bookmarkRepository.deleteByMemberIdAndProblemId(memberId, problemId);
    }
}
