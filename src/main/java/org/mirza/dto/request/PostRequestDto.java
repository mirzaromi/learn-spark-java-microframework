package org.mirza.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mirza.dto.pagination.PaginationRequestDto;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDto {
    @NotNull(message = "title must not be null")
    private String title;
    @NotNull(message = "content must not be null")
    private String content;
    private boolean isDeleted = false;
    private PaginationRequestDto paginationRequest;
}
