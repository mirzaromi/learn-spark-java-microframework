package org.mirza.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationDto<T> {
    private T data;
    private int page;
    private int size;
    private int totalData;
    private int totalPage;

}
