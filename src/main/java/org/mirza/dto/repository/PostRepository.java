package org.mirza.dto.repository;

import org.mirza.dto.pagination.PaginationRequestDto;
import org.mirza.entity.Post;
import spark.Response;

import java.util.List;

public interface PostRepository {
    List<Post> getAllPost(Response res, PaginationRequestDto paginationRequest);

    Integer countAllPost(Response res);

    Post findPostById(Response res, Integer requestId);

    Boolean insertPost(Response res, Post post);

    Boolean updatePost(Response res, Post post);
}
