package com.coursecanon.examaura.dto.supportingdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private PaginationInfo pagination;

    public static <T> PaginatedResponse<T> fromPage(Page<T> page){
        return PaginatedResponse.<T>builder()
                .data(page.getContent())
                .pagination(PaginationInfo.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .hasNext(page.hasNext())
                        .build())
                .build();
    }
}
