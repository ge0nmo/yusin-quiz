package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import com.cpa.yusin.quiz.problem.controller.port.GetProblemV2Service;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.domain.block.Block;
import com.cpa.yusin.quiz.problem.domain.block.ImageBlock;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetProblemV2ServiceImpl implements GetProblemV2Service
{
    private final ProblemRepository problemRepository;
    private final ChoiceService choiceService;
    private final FileService fileService;

    @Value("${cloud.aws.s3.prefix}")
    private String s3Prefix;

    @Override
    public ProblemV2Response getById(Long problemId)
    {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));

        List<ChoiceResponse> choices = choiceService.getAllByProblemId(problemId);

        return mapToResponse(problem, choices);
    }

    @Override
    public List<ProblemV2Response> getAllByExamId(Long examId)
    {
        List<Problem> problems = problemRepository.findAllByExamId(examId);

        // N+1 문제 방지를 위해 Choice를 한 번에 가져오거나, Batch Fetch 설정이 되어 있다고 가정
        return problems.stream()
                .map(problem -> {
                    List<ChoiceResponse> choices = choiceService.getAllByProblemId(problem.getId());
                    return mapToResponse(problem, choices);
                })
                .collect(Collectors.toList());
    }

    // =================================================================
    // Helper Methods
    // =================================================================

    private ProblemV2Response mapToResponse(Problem problem, List<ChoiceResponse> choices) {
        return ProblemV2Response.builder()
                .id(problem.getId())
                .number(problem.getNumber())
                .content(processBlocksWithPresignedUrl(problem.getContentJson()))
                .explanation(processBlocksWithPresignedUrl(problem.getExplanationJson()))
                .choices(choices)
                .build();
    }

    /**
     * Block 리스트를 순회하며 ImageBlock의 URL을 Presigned URL로 변환하여 반환
     * (원본 리스트를 수정하지 않고 새로운 리스트를 생성하여 반환)
     */
    private List<Block> processBlocksWithPresignedUrl(List<Block> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }

        return blocks.stream().map(block -> {
            if (block instanceof ImageBlock) {
                ImageBlock imgBlock = (ImageBlock) block;
                String originalSrc = imgBlock.getSrc();

                // S3 URL인 경우에만 서명된 URL로 교체
                if (isS3Url(originalSrc)) {
                    String signedUrl = generatePresignedUrl(originalSrc);
                    // ImageBlock은 불변 객체가 아니므로 새로 생성해서 리턴 (사이드 이펙트 방지)
                    return ImageBlock.builder()
                            .type("image")
                            .src(signedUrl)
                            .alt(imgBlock.getAlt())
                            .build();
                }
            }
            // TextBlock 등은 그대로 반환
            return block;
        }).collect(Collectors.toList());
    }

    private boolean isS3Url(String url) {
        return url != null && url.startsWith("http") && url.contains(s3Prefix);
    }

    private String generatePresignedUrl(String fullUrl) {
        try {
            String objectKey = extractObjectKeyFromUrl(fullUrl);
            return fileService.generatePresignedUrl(objectKey);
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", fullUrl, e);
            return fullUrl; // 실패 시 원본 반환 (혹은 에러 이미지)
        }
    }

    private String extractObjectKeyFromUrl(String fullUrl) {
        // URL에서 도메인 이후의 경로(Key)만 추출
        URI uri = URI.create(fullUrl);
        String path = uri.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1); // 맨 앞 '/' 제거
        }
        return URLDecoder.decode(path, StandardCharsets.UTF_8);
    }
}
