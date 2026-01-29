package com.cpa.yusin.quiz.global.init; // 혹은 service 패키지

import com.cpa.yusin.quiz.global.utils.HtmlToJsonConverter;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.domain.block.Block;

import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemMigrationService
{
    private final ProblemRepository problemRepository;
    private final HtmlToJsonConverter htmlToJsonConverter;

    @Transactional
    public String migrateHtmlToJson() {
        log.info("========== [API Trigger] Migration Start ==========");

        List<Problem> problems = problemRepository.findAll();
        int totalCount = problems.size();
        int successCount = 0;
        int skipCount = 0;

        for (Problem problem : problems) {
            try {
                // 이미 변환된 데이터 스킵
                if (problem.getContentJson() != null && !problem.getContentJson().isEmpty()) {
                    skipCount++;
                    continue;
                }

                String oldContent = problem.getContent() == null ? "" : problem.getContent();
                String oldExplanation = problem.getExplanation() == null ? "" : problem.getExplanation();

                List<Block> contentBlocks = htmlToJsonConverter.convert(oldContent);
                List<Block> explanationBlocks = htmlToJsonConverter.convert(oldExplanation);

                problem.migrateToBlocks(contentBlocks, explanationBlocks);
                successCount++;

            } catch (Exception e) {
                log.error("Error migrating problem id: {}", problem.getId(), e);
            }
        }

        String resultMsg = String.format("Migration Finished. Total: %d, Success: %d, Skipped: %d", totalCount, successCount, skipCount);
        log.info(resultMsg);

        return resultMsg;
    }
}