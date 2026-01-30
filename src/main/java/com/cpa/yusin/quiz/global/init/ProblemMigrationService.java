package com.cpa.yusin.quiz.global.init;

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
public class ProblemMigrationService {

    private final ProblemRepository problemRepository;
    private final HtmlToJsonConverter htmlToJsonConverter;

    /**
     * 전체 마이그레이션 실행
     * 대용량 데이터일 경우 @Transactional을 메서드 전체에 걸면 DB Lock 시간이 길어질 수 있으므로,
     * 청크 단위로 처리하거나 건별로 트랜잭션을 거는 것이 좋습니다.
     * 여기서는 간단하게 건별 처리를 위해 내부 루프에서 트랜잭션을 분리하지는 않았으나,
     * 실무에서는 Batch 처리를 고려해야 합니다.
     */
    @Transactional
    public String migrateHtmlToJson() {
        log.info("========== [Migration] Start HTML -> JSON Blocks ==========");

        List<Problem> problems = problemRepository.findAll();
        int totalCount = problems.size();
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        for (Problem problem : problems) {
            // 방어 로직: 이미 변환된 데이터는 스킵 (멱등성 보장)
            /*if (problem.getContentJson() != null && !problem.getContentJson().isEmpty()) {
                skipCount++;
                continue;
            }*/

            try {
                convertProblem(problem);
                successCount++;
            } catch (Exception e) {
                log.error("[Migration] Failed problem ID: {}", problem.getId(), e);
                failCount++;
                // 개별 실패가 전체 롤백을 유발하지 않게 하려면 여기서 catch하고 넘어갑니다.
            }
        }

        String resultMsg = String.format("Migration Finished. Total: %d, Success: %d, Skipped: %d, Failed: %d",
                totalCount, successCount, skipCount, failCount);
        log.info(resultMsg);
        return resultMsg;
    }

    // 단일 문제 변환 로직
    private void convertProblem(Problem problem) {
        String oldContent = problem.getContent() == null ? "" : problem.getContent();
        String oldExplanation = problem.getExplanation() == null ? "" : problem.getExplanation();

        List<Block> contentBlocks = htmlToJsonConverter.convert(oldContent);
        List<Block> explanationBlocks = htmlToJsonConverter.convert(oldExplanation);

        problem.migrateToBlocks(contentBlocks, explanationBlocks);

        // 로그가 너무 많다면 디버그 레벨로 변경
        log.debug("Migrated Problem ID: {}, Blocks: {}", problem.getId(), contentBlocks.size());
    }
}