package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemUpdateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.mapper.ProblemMapper;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProblemServiceImpl implements ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;
    private final ExamService examService;
    private final ChoiceService choiceService;
    private final ProblemValidator problemValidator;
    private final FileService fileService;

    @Value("${cloud.aws.s3.prefix}")
    private String s3Prefix;

    @Transactional
    @Override
    public void save(long examId, ProblemCreateRequest request) {
        Exam exam = examService.findById(examId);
        problemValidator.validateUniqueProblemNumber(examId, request.getNumber());

        // [수정] 문제 내용(Content)과 해설(Explanation)만 이미지 처리 (Choice 제외)
        String cleanContent = processHtmlImages(request.getContent());
        String cleanExplanation = processHtmlImages(request.getExplanation());

        request.setContent(cleanContent);
        request.setExplanation(cleanExplanation);

        Problem problem = problemMapper.toProblemEntity(request, exam);
        problem = problemRepository.save(problem);

        // Choice는 이미지 처리 없이 그대로 저장
        choiceService.save(problem, request.getChoices());
    }

    @Transactional
    @Override
    public void update(long problemId, ProblemUpdateRequest request, long examId) {
        Problem problem = findById(problemId);

        // [수정] 문제 내용과 해설만 이미지 처리
        String cleanContent = processHtmlImages(request.getContent());
        String cleanExplanation = processHtmlImages(request.getExplanation());

        problem.update(cleanContent, request.getNumber(), cleanExplanation);
        problemRepository.save(problem);
    }

    @Transactional
    @Override
    public ProblemDTO processSaveOrUpdate(ProblemRequest request, long examId) {
        // [수정] 통합 로직에서도 문제/해설만 처리
        request.setContent(processHtmlImages(request.getContent()));
        request.setExplanation(processHtmlImages(request.getExplanation()));

        Exam exam = examService.findById(examId);
        return request.isNew() ? save(request, exam) : update(request);
    }

    private ProblemDTO save(ProblemRequest request, Exam exam) {
        Problem problem = Problem.fromSaveOrUpdate(request.getContent(), request.getExplanation(), request.getNumber(),
                exam);
        problem = problemRepository.save(problem);
        List<Choice> choices = choiceService.saveOrUpdate(request.getChoices(), problem);
        return problemMapper.mapToProblemDTO(problem, choices);
    }

    private ProblemDTO update(ProblemRequest request) {
        Problem problem = findById(request.getId());
        problem.update(request.getContent(), request.getNumber(), request.getExplanation());
        problem = problemRepository.save(problem);
        List<Choice> choices = choiceService.saveOrUpdate(request.getChoices(), problem);
        return problemMapper.mapToProblemDTO(problem, choices);
    }

    @Override
    public GlobalResponse<List<ProblemDTO>> getAllByExamId(long examId) {
        List<Problem> problems = problemRepository.findAllByExamId(examId);
        Map<Long, List<ChoiceResponse>> choiceMap = choiceService.findAllByExamId(examId);

        List<ProblemDTO> response = problems.stream()
                .map(problem -> {
                    // [수정] 조회 시에도 문제/해설만 Presigned URL 변환
                    String signedContent = replaceImageSrcWithPresignedUrl(problem.getContent());
                    String signedExplanation = replaceImageSrcWithPresignedUrl(problem.getExplanation());

                    // Choice는 텍스트이므로 변환 로직 제거됨

                    return ProblemDTO.builder()
                            .id(problem.getId())
                            .number(problem.getNumber())
                            .content(signedContent)
                            .explanation(signedExplanation)
                            .choices(choiceMap.get(problem.getId()))
                            .build();
                })
                .toList();

        return new GlobalResponse<>(response);
    }

    @Override
    public ProblemDTO getById(long id) {
        Problem problem = findById(id);
        List<ChoiceResponse> choices = choiceService.getAllByProblemId(problem.getId());

        // [수정] 단건 조회 시 문제/해설만 변환
        String signedContent = replaceImageSrcWithPresignedUrl(problem.getContent());
        String signedExplanation = replaceImageSrcWithPresignedUrl(problem.getExplanation());

        return ProblemDTO.builder()
                .id(problem.getId())
                .number(problem.getNumber())
                .content(signedContent)
                .explanation(signedExplanation)
                .choices(choices)
                .build();
    }

    @Override
    public Problem findById(long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));
    }

    // ---------------------------------------------------------
    // 1. [저장용] Base64 -> S3 Upload & URL 변환
    // ---------------------------------------------------------
    private String processHtmlImages(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty())
            return htmlContent;

        Document doc = Jsoup.parseBodyFragment(htmlContent);
        Elements imgs = doc.select("img[src^=data:image]");

        for (Element img : imgs) {
            String src = img.attr("src");
            try {
                String[] parts = src.split(",");
                String header = parts[0];
                String data = parts[1];

                String extension = header.split(";")[0].split("/")[1];
                byte[] imageBytes = Base64.getDecoder().decode(data);

                String filename = UUID.randomUUID().toString() + "." + extension;

                // FileService 호출 (Byte Array 업로드)
                String s3Url = fileService.saveByteArray(imageBytes, filename, "image/" + extension);

                img.attr("src", s3Url);
                img.attr("style", "max-width: 100%; height: auto;");

            } catch (Exception e) {
                log.error("Base64 이미지 변환 실패", e);
            }
        }
        return doc.body().html();
    }

    // ---------------------------------------------------------
    // 2. [조회용] S3 URL -> Presigned URL 변환
    // ---------------------------------------------------------
    private String replaceImageSrcWithPresignedUrl(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty())
            return htmlContent;

        Document doc = Jsoup.parseBodyFragment(htmlContent);
        Elements imgs = doc.select("img");

        for (Element img : imgs) {
            String src = img.attr("src");
            if (src.contains("amazonaws.com") && src.contains(s3Prefix)) {
                try {
                    String objectKey = extractObjectKeyFromUrl(src);
                    String presignedUrl = fileService.generatePresignedUrl(objectKey);
                    if (presignedUrl != null && !presignedUrl.isEmpty()) {
                        img.attr("src", presignedUrl);
                    }
                } catch (Exception e) {
                    log.error("이미지 변환 실패: {}", src, e);
                }
            }
        }
        return doc.body().html();
    }

    private String extractObjectKeyFromUrl(String fullUrl) {
        URI uri = URI.create(fullUrl);
        String path = uri.getPath();
        if (path.startsWith("/"))
            path = path.substring(1);
        return URLDecoder.decode(path, StandardCharsets.UTF_8);
    }
}