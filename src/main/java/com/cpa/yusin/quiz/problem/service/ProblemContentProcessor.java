package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.problem.domain.block.Block;
import com.cpa.yusin.quiz.problem.domain.block.ImageBlock;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProblemContentProcessor {

    private final FileService fileService;
    private final String s3Prefix;

    public ProblemContentProcessor(FileService fileService, @Value("${cloud.aws.s3.prefix}") String s3Prefix) {
        this.fileService = fileService;
        this.s3Prefix = s3Prefix;
    }

    /**
     * Block 리스트를 순회하며 ImageBlock의 URL을 Presigned URL로 변환하여 반환
     * (V2 JSON Block 처리용)
     */
    public List<Block> processBlocksWithPresignedUrl(List<Block> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }

        return blocks.stream().map(block -> {
            if (block instanceof ImageBlock imgBlock) {
                String originalSrc = imgBlock.getSrc();

                if (isS3Url(originalSrc)) {
                    String signedUrl = generatePresignedUrl(originalSrc);
                    return ImageBlock.builder()
                            .type("image")
                            .src(signedUrl)
                            .alt(imgBlock.getAlt())
                            .build();
                }
            }
            return block;
        }).collect(Collectors.toList());
    }

    /**
     * HTML 문자열 내의 img 태그 src를 Presigned URL로 변환하여 반환
     * (V1 Legacy HTML 처리용)
     */
    public String processHtmlWithPresignedUrl(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }

        Document doc = Jsoup.parseBodyFragment(htmlContent);
        Elements imgs = doc.select("img");
        boolean modified = false;

        for (var img : imgs) {
            String src = img.attr("src");
            if (isS3Url(src)) {
                String signedUrl = generatePresignedUrl(src);
                if (signedUrl != null && !signedUrl.isEmpty()) {
                    img.attr("src", signedUrl);
                    modified = true;
                }
            }
        }

        return modified ? doc.body().html() : htmlContent;
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
            return fullUrl;
        }
    }

    private String extractObjectKeyFromUrl(String fullUrl) {
        URI uri = URI.create(fullUrl);
        String path = uri.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return URLDecoder.decode(path, StandardCharsets.UTF_8);
    }
}
