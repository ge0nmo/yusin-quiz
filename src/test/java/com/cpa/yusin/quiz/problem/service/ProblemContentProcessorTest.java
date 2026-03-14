package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.problem.domain.block.Block;
import com.cpa.yusin.quiz.problem.domain.block.ImageBlock;
import com.cpa.yusin.quiz.problem.domain.block.ListBlock;
import com.cpa.yusin.quiz.problem.domain.block.ListItemBlock;
import com.cpa.yusin.quiz.problem.domain.block.TextBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ProblemContentProcessorTest {

    private ProblemContentProcessor processor;
    private FileService fileService;
    private final String S3_PREFIX = "test-prefix";

    @BeforeEach
    void setUp() {
        fileService = Mockito.mock(FileService.class);
        processor = new ProblemContentProcessor(fileService, S3_PREFIX);
    }

    @Test
    @DisplayName("Block 리스트 S3 URL 변환 성공")
    void processBlocksWithPresignedUrl_success() {
        // given
        String originalUrl = "https://s3.amazonaws.com/" + S3_PREFIX + "/image.png";
        String presignedUrl = "https://s3.amazonaws.com/presigned-url";

        when(fileService.generatePresignedUrl(anyString())).thenReturn(presignedUrl);

        List<Block> blocks = List.of(
                TextBlock.builder().type("text").tag("p").build(),
                ImageBlock.builder().type("image").src(originalUrl).alt("alt").build());

        // when
        List<Block> result = processor.processBlocksWithPresignedUrl(blocks);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isInstanceOf(TextBlock.class);

        assertThat(result.get(1)).isInstanceOf(ImageBlock.class);
        ImageBlock imgBlock = (ImageBlock) result.get(1);
        assertThat(imgBlock.getSrc()).isEqualTo(presignedUrl);
    }

    @Test
    @DisplayName("HTML S3 URL 변환 성공")
    void processHtmlWithPresignedUrl_success() {
        // given
        String originalUrl = "https://s3.amazonaws.com/" + S3_PREFIX + "/image.png";
        String presignedUrl = "https://s3.amazonaws.com/presigned-url";
        String html = "<div><img src=\"" + originalUrl + "\"></div>";

        when(fileService.generatePresignedUrl(anyString())).thenReturn(presignedUrl);

        // when
        String result = processor.processHtmlWithPresignedUrl(html);

        // then
        assertThat(result).contains(presignedUrl);
        assertThat(result).doesNotContain(originalUrl);
    }

    @Test
    @DisplayName("S3 URL이 아닌 경우 변환하지 않음")
    void processBlocks_ignore_nonS3() {
        // given
        String normalUrl = "https://google.com/image.png";
        List<Block> blocks = List.of(
                ImageBlock.builder().type("image").src(normalUrl).alt("alt").build());

        // when
        List<Block> result = processor.processBlocksWithPresignedUrl(blocks);

        // then
        ImageBlock imgBlock = (ImageBlock) result.get(0);
        assertThat(imgBlock.getSrc()).isEqualTo(normalUrl);
    }

    @Test
    @DisplayName("중첩 리스트 안의 이미지도 presigned URL 로 변환한다")
    void processBlocks_shouldTraverseNestedListBlocks() {
        String originalUrl = "https://s3.amazonaws.com/" + S3_PREFIX + "/nested.png";
        String presignedUrl = "https://s3.amazonaws.com/presigned-nested-url";

        when(fileService.generatePresignedUrl(anyString())).thenReturn(presignedUrl);

        List<Block> blocks = List.of(
                ListBlock.builder()
                        .type("list")
                        .ordered(false)
                        .children(List.of(
                                ListItemBlock.builder()
                                        .type("listItem")
                                        .children(List.of(
                                                ImageBlock.builder().type("image").src(originalUrl).alt("nested").build()
                                        ))
                                        .build()
                        ))
                        .build()
        );

        List<Block> result = processor.processBlocksWithPresignedUrl(blocks);

        ListBlock listBlock = (ListBlock) result.getFirst();
        ListItemBlock listItemBlock = listBlock.getChildren().getFirst();
        ImageBlock imageBlock = (ImageBlock) listItemBlock.getChildren().getFirst();
        assertThat(imageBlock.getSrc()).isEqualTo(presignedUrl);
    }
}
