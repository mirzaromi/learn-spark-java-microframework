package org.mirza.controller;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.mirza.dto.BaseResponse;
import org.mirza.dto.pagination.PaginationDto;
import org.mirza.dto.pagination.PaginationRequestDto;
import org.mirza.dto.request.PostRequestDto;
import org.mirza.dto.response.PostResponseDto;
import org.mirza.entity.Post;
import org.mirza.exception.DatabaseException;
import org.mirza.repository.PostRepositoryImpl;
import org.mirza.service.PostService;
import org.mirza.util.PaginationUtil;
import org.mirza.util.ValidatorUtil;
import org.modelmapper.ModelMapper;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Optional;

@Slf4j
public class PostController {
    private static final PostService postService = new PostService(new PostRepositoryImpl(), new ModelMapper());
    private static final ModelMapper modelMapper = new ModelMapper();
    private static final Gson gson = new Gson();


    public static String getAllPosts(Request req, Response res) {
        PaginationRequestDto paginationRequest = PaginationUtil.getPaginationRequestDto(req);

        PostRequestDto postRequestDto = modelMapper.map(req, PostRequestDto.class);
        postRequestDto.setPaginationRequest(paginationRequest);

        PaginationDto<Object> paginationDto = postService.getPosts(postRequestDto);

        var successGetAllPosts = BaseResponse.generateSuccessResponse("Success get all posts", paginationDto);

        // response status
        res.status(200);

        return gson.toJson(successGetAllPosts);
    }

    public static String getPostById(Request req, Response res) {
        Integer requestId  = Integer.parseInt(req.params(":id"));

        PostResponseDto postResponseDto = postService.getPostById(requestId);

        BaseResponse<PostResponseDto> response = BaseResponse.generateSuccessResponse("Success get post", postResponseDto);
        // response status
        res.status(200);

        return gson.toJson(response);
    }

    public static String createPost(Request req, Response res) {
        Post post = ValidatorUtil.parseAndValidatePostRequest(req, gson);

        PostResponseDto postResponseDto = postService.createPost(post);

        res.status(HttpStatus.CREATED_201);

        BaseResponse<PostResponseDto> successCreateAPost = BaseResponse.generateResponse(HttpStatus.CREATED_201, "Success create a post", postResponseDto);
        return gson.toJson(successCreateAPost);
    }

    public static String updatePost(Request req, Response res) {
        Integer requestId  = Integer.parseInt(req.params(":id"));

        Post newPost = ValidatorUtil.parseAndValidatePostRequest(req, gson);
        PostResponseDto postResponseDto = postService.updatePost(requestId, newPost);

        res.status(HttpStatus.OK_200);

        BaseResponse<PostResponseDto> successCreateAPost = BaseResponse.generateResponse(HttpStatus.OK_200, "Success update a post", postResponseDto);
        return gson.toJson(successCreateAPost);
    }

    public static String deletePost(Request req, Response res) {
        Integer id  = Integer.parseInt(req.params(":id"));

        PostResponseDto postResponseDto = postService.deletePost(id);

        res.status(HttpStatus.OK_200);

        BaseResponse<PostResponseDto> successCreateAPost = BaseResponse.generateResponse(HttpStatus.OK_200, "Success delete a post", postResponseDto);
        return gson.toJson(successCreateAPost);
    }
}
