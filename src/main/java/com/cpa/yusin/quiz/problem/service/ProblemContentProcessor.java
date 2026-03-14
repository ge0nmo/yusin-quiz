package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.problem.domain.block.Block;
import com.cpa.yusin.quiz.problem.domain.block.ImageBlock;
import com.cpa.yusin.quiz.problem.domain.block.ListBlock;
import com.cpa.yusin.quiz.problem.domain.block.ListItemBlock;
import com.cpa.yusin.quiz.problem.domain.block.TextBlock;
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

@Slf4j
@Component
public class ProblemContentProcessor {

    private final FileService fileService;
    private final String s3Prefix;

    public ProblemContentProcessor(FileService fileService, @Value("${cloud.aws.s3.prefix}") String s3Prefix) {
        this.fileService = fileService;
        this.s3Prefix = s3Prefix;
    }

    public List<Block> processBlocksWithPresignedUrl(List<Block> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }

        return blocks.stream()
                .map(this::mapBlock)
                .toList();
    }

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

    private Block mapBlock(Block block) {
        if (block instanceof ImageBlock imageBlock) {
            return mapImageBlock(imageBlock);
        }

        if (block instanceof ListBlock listBlock) {
            return ListBlock.builder()
                    .type(listBlock.getType())
                    .align(listBlock.getAlign())
                    .ordered(listBlock.isOrdered())
                    .children(listBlock.getChildren().stream()
                            .map(this::mapListItemBlock)
                            .toList())
                    .build();
        }

        if (block instanceof ListItemBlock listItemBlock) {
            return mapListItemBlock(listItemBlock);
        }

        if (block instanceof TextBlock textBlock) {
            return TextBlock.builder()
                    .type(textBlock.getType())
                    .align(textBlock.getAlign())
                    .tag(textBlock.getTag())
                    .spans(textBlock.getSpans())
                    .build();
        }

        return block;
    }

    private ListItemBlock mapListItemBlock(ListItemBlock listItemBlock) {
        return ListItemBlock.builder()
                .type(listItemBlock.getType())
                .align(listItemBlock.getAlign())
                .children(listItemBlock.getChildren().stream()
                        .map(this::mapBlock)
                        .toList())
                .build();
    }

    private ImageBlock mapImageBlock(ImageBlock imageBlock) {
        String source = imageBlock.getSrc();
        String resolvedSource = isS3Url(source) ? generatePresignedUrl(source) : source;

        return ImageBlock.builder()
                .type(imageBlock.getType())
                .align(imageBlock.getAlign())
                .src(resolvedSource)
                .alt(imageBlock.getAlt())
                .build();
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
