package org.mirza.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    private Integer id;
    @NotNull(message = "title must not be null")
    private String title;
    @NotNull(message = "content must not be null")
    private String content;
    private boolean isDeleted = false;
}
