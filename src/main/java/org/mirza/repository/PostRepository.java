package org.mirza.repository;

import org.mirza.dto.pagination.PaginationRequestDto;
import org.mirza.dto.response.PostResponseDto;
import org.mirza.entity.Post;
import spark.Response;

import java.util.List;

public interface PostRepository {
    List<Post> getAllPost(PaginationRequestDto paginationRequest);

    Integer countAllPost();

    Post findPostById(Integer requestId);

    Boolean insertPost(Post post);

    Boolean updatePost(Post post);

    Boolean insertBulkPost(List<Post> posts);
}
