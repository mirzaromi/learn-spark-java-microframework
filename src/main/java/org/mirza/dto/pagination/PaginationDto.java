package org.mirza.dto.pagination;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationDto<T> {
    private T data;
    private int page;
    private int size;
    private int totalData;
    private int totalPage;

}
