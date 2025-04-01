package org.mirza;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.mirza.dto.BaseResponse;
import org.mirza.dto.ErrorResponse;
import org.mirza.dto.pagination.PaginationDto;
import org.mirza.dto.pagination.PaginationRequestDto;
import org.mirza.dto.repository.PostRepositoryImpl;
import org.mirza.dto.response.PostResponseDto;
import org.mirza.entity.Post;
import org.mirza.exception.DatabaseException;
import org.mirza.util.ValidatorUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.util.*;

import static spark.Spark.*;

@Slf4j
public class Main {

    public static final String DATABASE_ERROR = "Database error";
    static Map<Integer, Post> database = new TreeMap<>();
    private final static PostRepositoryImpl postRepository = new PostRepositoryImpl();

    public static void main(String[] args) {

        Gson gson = new Gson();


        ModelMapper modelMapper = new ModelMapper();


        // create static data using MAP
        // Map with key id int and value is list of object (entity)

        port(8080);

        before((req, res) -> {
            // Logic before the route handler
            log.info("Request received: {} {}", req.requestMethod(), req.uri());
        });

        after((req, res) -> {
            // Logic after the route handler (e.g., logging or cleaning up resources)
            res.header("content-type", "application/json");

            log.info("Response sent: {}", res.status());
            log.info("Response content: {}", res.body());
        });

        exception(Exception.class, (exception, req, res) -> {
            log.warn(exception.getMessage());
            res.status(500);
            res.type("application/json");
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, exception.getMessage());
            res.body(gson.toJson(errorResponse));
        });


        // get all post
        get("/", (req, res) -> {
            int page = Optional.ofNullable(req.queryParams("page")).map(Integer::parseInt).orElse(1);
            int size = Optional.ofNullable(req.queryParams("size")).map(Integer::parseInt).orElse(10);
            String orderBy = Optional.ofNullable(req.queryParams("orderBy")).map(String::trim).orElse("id");

            PaginationRequestDto paginationRequest = new PaginationRequestDto(page, size, orderBy);

            List<Post> posts = postRepository.getAllPost(res, paginationRequest);

            int totalData = postRepository.countAllPost(res);

            int totalPage = (int) Math.ceil((double) totalData / size);

            List<PostResponseDto> postResponseDtos = modelMapper.map(posts, new TypeToken<List<PostResponseDto>>(){}.getType());

            PaginationDto<Object> paginationDto = PaginationDto.builder()
                    .page(page)
                    .size(size)
                    .totalData(totalData)
                    .totalPage(totalPage)
                    .data(postResponseDtos)
                    .build();



            var successGetAllPosts = BaseResponse.generateSuccessResponse("Success get all posts", paginationDto);
            return gson.toJson(successGetAllPosts);
        });

        // get post by Id
        get("/:id", (req, res) -> {
            Integer requestId  = Integer.parseInt(req.params(":id"));

            Post post = postRepository.findPostById(res, requestId);

            PostResponseDto postResponseDto = modelMapper.map(post, PostResponseDto.class);

            BaseResponse<PostResponseDto> response = BaseResponse.generateSuccessResponse("Success get post", postResponseDto);

            return gson.toJson(response);
        });

        // create post
        post("/", (req, res) -> {

            Post post = ValidatorUtil.parseAndValidatePostRequest(req, gson);

            Boolean isSuccessDBOperation = postRepository.insertPost(res, post);

            if (Boolean.TRUE.equals(isSuccessDBOperation)) {
                PostResponseDto postResponseDto = modelMapper.map(post, PostResponseDto.class);

                log.info("Suucess create post with id : {}", post.getId());

                res.status(HttpStatus.CREATED_201);

                BaseResponse<PostResponseDto> successCreateAPost = BaseResponse.generateResponse(HttpStatus.CREATED_201, "Success create a post", postResponseDto);
                return gson.toJson(successCreateAPost);
            }

            throw new DatabaseException("Failed to create post.");

        });

        // update post
        put("/:id", (req, res) -> {
            Integer requestId  = Integer.parseInt(req.params(":id"));

            Post post = postRepository.findPostById(res, requestId);

            Post newPost = ValidatorUtil.parseAndValidatePostRequest(req, gson);

            post.setTitle(newPost.getTitle());
            post.setContent(newPost.getContent());

            Boolean isSuccessDBOperation = postRepository.updatePost(res, post);

            if (Boolean.TRUE.equals(isSuccessDBOperation)) {
                PostResponseDto postResponseDto = modelMapper.map(post, PostResponseDto.class);

                log.info("Suucess update post with id : {}", post.getId());

                res.status(HttpStatus.OK_200);

                BaseResponse<PostResponseDto> successCreateAPost = BaseResponse.generateResponse(HttpStatus.OK_200, "Success update a post", postResponseDto);
                return gson.toJson(successCreateAPost);
            }

            throw new DatabaseException("Failed to update post.");
        });

        // delete post
        delete("/:id", (req, res) -> {

            Integer id  = Integer.parseInt(req.params(":id"));
            Post post = postRepository.findPostById(res, id);

            post.setDeleted(true);

            Boolean isSuccessDBOperation = postRepository.updatePost(res, post);

            if (Boolean.TRUE.equals(isSuccessDBOperation)) {
                PostResponseDto postResponseDto = modelMapper.map(post, PostResponseDto.class);

                log.info("Suucess delete post with id : {}", post.getId());

                res.status(HttpStatus.OK_200);

                BaseResponse<PostResponseDto> successCreateAPost = BaseResponse.generateResponse(HttpStatus.OK_200, "Success delete a post", postResponseDto);
                return gson.toJson(successCreateAPost);
            }

            throw new DatabaseException("Failed to delete post.");
        });

    }
}
