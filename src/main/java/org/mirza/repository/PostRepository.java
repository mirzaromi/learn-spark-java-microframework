package org.mirza.repository;

import org.mirza.dto.pagination.PaginationRequestDto;
import org.mirza.dto.response.PostResponseDto;
import org.mirza.entity.Post;
import spark.Response;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    List<Post> getAllPost(PaginationRequestDto paginationRequest);

    Integer countAllPost();

    Optional<Post> findPostById(Integer requestId);

    public void save(Post post);

    public void saveAll(List<Post> post);

    public void saveAndFlush(Post post);
}
