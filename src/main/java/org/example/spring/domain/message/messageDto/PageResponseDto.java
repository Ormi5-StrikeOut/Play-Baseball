package org.example.spring.domain.message.messageDto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
public class PageResponseDto {

    private Object data;
    private PageInfo pageInfo;

    @Data
    @Builder
    public static class PageInfo {
        private int page;
        private int size;
        private Long totalElements;
        private int totalPages;
    }

    public static PageResponseDto of(Object list, Page<?> page) {
        PageInfo pageInfoResponse = PageInfo.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return PageResponseDto.builder()
                .data(list)
                .pageInfo(pageInfoResponse)
                .build();
    }
}
