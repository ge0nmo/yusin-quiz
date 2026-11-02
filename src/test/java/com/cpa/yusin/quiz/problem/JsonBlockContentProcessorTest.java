package com.cpa.yusin.quiz.problem;

import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.problem.service.JsonBlockContentProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JsonBlockContentProcessorTest {
    @Test
    void refreshesManagedImageUrlsInsideNestedJsonBlocks() {
        FileService fileService = mock(FileService.class);
        when(fileService.generatePresignedUrl("post/image one.png")).thenReturn("https://signed/new");
        JsonBlockContentProcessor processor = new JsonBlockContentProcessor(fileService);
        ReflectionTestUtils.setField(processor, "s3Prefix", "post");

        List<Map<String, Object>> processed = processor.withFreshImageUrls(List.of(Map.of(
                "type", "statementGroup", "items", List.of(Map.of(
                        "label", "(가)", "content", List.of(Map.of(
                                "type", "image",
                                "src", "https://bucket.s3.amazonaws.com/post/image%20one.png?old=true")))))));

        Map<?, ?> item = (Map<?, ?>) ((List<?>) processed.getFirst().get("items")).getFirst();
        Map<?, ?> image = (Map<?, ?>) ((List<?>) item.get("content")).getFirst();
        assertThat(image.get("src")).isEqualTo("https://signed/new");
        verify(fileService).generatePresignedUrl("post/image one.png");
    }

    @Test
    void refreshesManagedImageUrlsInsideTableCells() {
        FileService fileService = mock(FileService.class);
        when(fileService.generatePresignedUrl("post/table.png")).thenReturn("https://signed/table");
        JsonBlockContentProcessor processor = new JsonBlockContentProcessor(fileService);
        ReflectionTestUtils.setField(processor, "s3Prefix", "post");

        List<Map<String, Object>> processed = processor.withFreshImageUrls(List.of(Map.of(
                "type", "table", "rows", List.of(Map.of("cells", List.of(Map.of(
                        "header", true, "colspan", 2, "content", List.of(Map.of(
                                "type", "image",
                                "src", "https://bucket.s3.amazonaws.com/post/table.png?old=true")))))))));

        Map<?, ?> row = (Map<?, ?>) ((List<?>) processed.getFirst().get("rows")).getFirst();
        Map<?, ?> cell = (Map<?, ?>) ((List<?>) row.get("cells")).getFirst();
        Map<?, ?> image = (Map<?, ?>) ((List<?>) cell.get("content")).getFirst();
        assertThat(image.get("src")).isEqualTo("https://signed/table");
        assertThat(cell.get("header")).isEqualTo(true);
        assertThat(cell.get("colspan")).isEqualTo(2);
    }

    @Test
    void leavesExternalImagesUntouched() {
        FileService fileService = mock(FileService.class);
        JsonBlockContentProcessor processor = new JsonBlockContentProcessor(fileService);
        ReflectionTestUtils.setField(processor, "s3Prefix", "post");

        List<Map<String, Object>> processed = processor.withFreshImageUrls(List.of(
                Map.of("type", "image", "src", "https://example.com/image.png")));

        assertThat(processed.getFirst().get("src")).isEqualTo("https://example.com/image.png");
        verifyNoInteractions(fileService);
    }
}
