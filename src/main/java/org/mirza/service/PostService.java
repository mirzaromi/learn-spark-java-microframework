package org.mirza.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.mirza.dto.pagination.PaginationDto;
import org.mirza.dto.pagination.PaginationRequestDto;
import org.mirza.dto.request.PostRequestDto;
import org.mirza.dto.response.PostResponseDto;
import org.mirza.entity.Post;
import org.mirza.exception.DatabaseException;
import org.mirza.exception.NotFoundException;
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
        Post post = postRepository.findPostById(requestId)
                .orElseThrow(() -> new NotFoundException("Post with id " + requestId + " not found."));

        return modelMapper.map(post, PostResponseDto.class);
    }

    @Transactional
    public PostResponseDto createPost(Post post) {
        postRepository.save(post);

        log.info("Success create post with id : {}", post.getId());

        return modelMapper.map(post, PostResponseDto.class);
    }

    @Transactional
    public PostResponseDto updatePost(Integer requestId, Post newPost) {
        Post post = postRepository.findPostById(requestId)
                .orElseThrow(() -> new NotFoundException("Post with id " + requestId + " not found."));

        post.setTitle(newPost.getTitle());
        post.setContent(newPost.getContent());

        postRepository.save(post);

        log.info("Success update post with id : {}", post.getId());

        return modelMapper.map(post, PostResponseDto.class);
    }

    @Transactional
    public PostResponseDto deletePost(Integer requestId) {
        Post post = postRepository.findPostById(requestId)
                .orElseThrow(() -> new NotFoundException("Post with id " + requestId + " not found."));

        post.setDeleted(true);

       postRepository.save(post);

        log.info("Success delete post with id : {}", post.getId());

        return modelMapper.map(post, PostResponseDto.class);
    }

    @Transactional
    public List<PostResponseDto> createBulkPost(List<Post> posts) {
        postRepository.saveAll(posts);

        log.info("Success create {} posts", posts.size());

        return modelMapper.map(posts, new TypeToken<List<PostResponseDto>>(){}.getType());
    }
}
