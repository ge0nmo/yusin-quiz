package com.cpa.yusin.quiz.common.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@AllArgsConstructor
@Data
@Builder
public class PageInfo {
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public static PageInfo of(Page<?> page) {
        return PageInfo.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getPageable().getPageNumber())
                .pageSize(page.getPageable().getPageSize())
                .build();
    }
}
