package org.mirza.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationRequestDto {
    private int page;
    private int size;
    private String orderBy;
    private int offset;

    public PaginationRequestDto(int page, int size, String orderBy) {
        this.page = page;
        this.size = size;
        this.orderBy = orderBy;
        this.offset = (page - 1) * size;
    }
}
