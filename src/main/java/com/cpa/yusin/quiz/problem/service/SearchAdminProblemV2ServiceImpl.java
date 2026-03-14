package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.problem.controller.dto.response.AdminProblemSearchResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemLectureResponse;
import com.cpa.yusin.quiz.problem.controller.port.SearchAdminProblemV2Service;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.domain.block.Block;
import com.cpa.yusin.quiz.problem.domain.block.ListBlock;
import com.cpa.yusin.quiz.problem.domain.block.ListItemBlock;
import com.cpa.yusin.quiz.problem.domain.block.Span;
import com.cpa.yusin.quiz.problem.domain.block.TextBlock;
import com.cpa.yusin.quiz.problem.service.dto.AdminProblemSearchCondition;
import com.cpa.yusin.quiz.problem.service.dto.AdminProblemSearchProjection;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SearchAdminProblemV2ServiceImpl implements SearchAdminProblemV2Service {

    private static final int PREVIEW_MAX_LENGTH = 120;

    private final ProblemRepository problemRepository;

    @Override
    public Page<AdminProblemSearchResponse> search(Pageable pageable, AdminProblemSearchCondition searchCondition) {
        AdminProblemSearchCondition normalizedCondition = searchCondition == null
                ? AdminProblemSearchCondition.of(null, null, null, null)
                : AdminProblemSearchCondition.of(
                        searchCondition.lectureStatus(),
                        searchCondition.subjectId(),
                        searchCondition.year(),
                        searchCondition.examId()
                );

        return problemRepository.searchAdminProblems(pageable, normalizedCondition)
                .map(this::toResponse);
    }

    private AdminProblemSearchResponse toResponse(AdminProblemSearchProjection projection) {
        Problem problem = projection.problem();

        return new AdminProblemSearchResponse(
                problem.getId(),
                problem.getNumber(),
                projection.subjectId(),
                projection.subjectName(),
                projection.examId(),
                projection.examName(),
                projection.examYear(),
                ProblemLectureResponse.from(problem),
                projection.choiceCount(),
                projection.answerChoiceCount(),
                buildContentPreview(problem)
        );
    }

    private String buildContentPreview(Problem problem) {
        String blockText = extractBlockText(problem.getContentJson());
        if (StringUtils.hasText(blockText)) {
            return abbreviate(blockText);
        }

        if (!StringUtils.hasText(problem.getContent())) {
            return "";
        }

        return abbreviate(Jsoup.parseBodyFragment(problem.getContent()).text());
    }

    private String extractBlockText(List<Block> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return "";
        }

        StringBuilder previewBuilder = new StringBuilder();
        appendBlockText(previewBuilder, blocks);

        return previewBuilder.toString().trim();
    }

    private void appendBlockText(StringBuilder previewBuilder, List<Block> blocks) {
        for (Block block : blocks) {
            if (block instanceof TextBlock textBlock) {
                appendText(previewBuilder, extractTextBlock(textBlock));
                continue;
            }

            if (block instanceof ListBlock listBlock) {
                for (ListItemBlock child : listBlock.getChildren()) {
                    appendBlockText(previewBuilder, child.getChildren());
                }
                continue;
            }

            if (block instanceof ListItemBlock listItemBlock) {
                appendBlockText(previewBuilder, listItemBlock.getChildren());
            }
        }
    }

    private String extractTextBlock(TextBlock textBlock) {
        if (textBlock.getSpans() == null || textBlock.getSpans().isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (Span span : textBlock.getSpans()) {
            if (span == null || !StringUtils.hasText(span.getText())) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(span.getText().trim());
        }

        return builder.toString();
    }

    private void appendText(StringBuilder previewBuilder, String text) {
        if (!StringUtils.hasText(text)) {
            return;
        }

        if (previewBuilder.length() > 0) {
            previewBuilder.append(' ');
        }
        previewBuilder.append(text.trim());
    }

    private String abbreviate(String source) {
        String normalized = source.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= PREVIEW_MAX_LENGTH) {
            return normalized;
        }

        return normalized.substring(0, PREVIEW_MAX_LENGTH - 3) + "...";
    }
}
