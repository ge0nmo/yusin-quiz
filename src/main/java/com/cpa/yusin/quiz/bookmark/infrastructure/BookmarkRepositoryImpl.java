package com.cpa.yusin.quiz.bookmark.infrastructure;

import com.cpa.yusin.quiz.bookmark.domain.Bookmark;
import com.cpa.yusin.quiz.bookmark.service.port.BookmarkRepository;
import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import com.cpa.yusin.quiz.subject.domain.SubjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class BookmarkRepositoryImpl implements BookmarkRepository {

    private final BookmarkJpaRepository bookmarkJpaRepository;

    @Override
    public Bookmark save(Bookmark bookmark) {
        return bookmarkJpaRepository.save(bookmark);
    }

    @Override
    public void deleteByMemberIdAndProblemId(Long memberId, Long problemId) {
        bookmarkJpaRepository.deleteByMemberIdAndProblemId(memberId, problemId);
    }

    @Override
    public boolean existsByMemberIdAndProblemId(Long memberId, Long problemId) {
        return bookmarkJpaRepository.existsByMemberIdAndProblemId(memberId, problemId);
    }

    @Override
    public Optional<Bookmark> findByMemberIdAndProblemId(Long memberId, Long problemId) {
        return bookmarkJpaRepository.findByMemberIdAndProblemId(memberId, problemId);
    }

    @Override
    public Slice<Bookmark> findByMemberIdAndSubjectId(Long memberId, Long subjectId, Pageable pageable) {
        if (subjectId == null) {
            return bookmarkJpaRepository.findAllByMemberId(memberId, SubjectStatus.PUBLISHED, ExamStatus.PUBLISHED, pageable);
        }
        return bookmarkJpaRepository.findAllByMemberIdAndSubjectId(memberId, subjectId, SubjectStatus.PUBLISHED, ExamStatus.PUBLISHED, pageable);
    }
}
