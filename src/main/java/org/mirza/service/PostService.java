package org.mirza.service;

import lombok.extern.slf4j.Slf4j;
import org.mirza.dto.pagination.PaginationDto;
import org.mirza.dto.pagination.PaginationRequestDto;
import org.mirza.dto.request.PostRequestDto;
import org.mirza.dto.response.PostResponseDto;
import org.mirza.entity.Post;
import org.mirza.exception.DatabaseException;
import org.mirza.repository.PostRepository;
import org.mirza.util.ValidatorUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.util.List;

@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    public PostService(PostRepository postRepository, ModelMapper modelMapper) {
        this.postRepository = postRepository;
        this.modelMapper = modelMapper;
    }

    public PaginationDto<Object> getPosts(PostRequestDto postRequestDto) {

        PaginationRequestDto paginationRequest = new PaginationRequestDto(postRequestDto.getPaginationRequest().getPage(), postRequestDto.getPaginationRequest().getSize(), postRequestDto.getPaginationRequest().getOrderBy());

        List<Post> posts = postRepository.getAllPost(paginationRequest);

        int totalData = postRepository.countAllPost();

        int totalPage = (int) Math.ceil((double) totalData / postRequestDto.getPaginationRequest().getSize());

        List<PostResponseDto> postResponseDtos = modelMapper.map(posts, new TypeToken<List<PostResponseDto>>(){}.getType());

        return PaginationDto.builder()
                .page(postRequestDto.getPaginationRequest().getPage())
                .size(postRequestDto.getPaginationRequest().getSize())
                .totalData(totalData)
                .totalPage(totalPage)
                .data(postResponseDtos)
                .build();
    }

    public PostResponseDto getPostById(Integer requestId) {
        Post post = postRepository.findPostById(requestId);

        return modelMapper.map(post, PostResponseDto.class);
    }

    public PostResponseDto createPost(Post post) {
        Boolean isSuccessDBOperation = postRepository.insertPost(post);

        if (Boolean.FALSE.equals(isSuccessDBOperation))
            throw new DatabaseException("Failed to create post.");

        log.info("Success create post with id : {}", post.getId());

        return modelMapper.map(post, PostResponseDto.class);
    }

    public PostResponseDto updatePost(Integer requestId, Post newPost) {
        Post post = postRepository.findPostById(requestId);

        post.setTitle(newPost.getTitle());
        post.setContent(newPost.getContent());

        Boolean isSuccessDBOperation = postRepository.updatePost(post);

        if (Boolean.FALSE.equals(isSuccessDBOperation))
            throw new DatabaseException("Failed to update post.");

        log.info("Success update post with id : {}", post.getId());

        return modelMapper.map(post, PostResponseDto.class);
    }

    public PostResponseDto deletePost(Integer requestId) {
        Post post = postRepository.findPostById(requestId);

        post.setDeleted(true);

        Boolean isSuccessDBOperation = postRepository.updatePost(post);

        if (Boolean.FALSE.equals(isSuccessDBOperation))
            throw new DatabaseException("Failed to delete post.");

        log.info("Success delete post with id : {}", post.getId());

        return modelMapper.map(post, PostResponseDto.class);
    }

    public List<PostResponseDto> createBulkPost(List<Post> posts) {
        Boolean isSuccessDBOperation = postRepository.insertBulkPost(posts);

        if (Boolean.FALSE.equals(isSuccessDBOperation))
            throw new DatabaseException("Failed to create post.");

        log.info("Success create {} posts", posts.size());

        return modelMapper.map(posts, new TypeToken<List<PostResponseDto>>(){}.getType());
    }
}
