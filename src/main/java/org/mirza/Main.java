package org.mirza;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.mirza.dto.BaseResponse;
import org.mirza.dto.ErrorResponse;
import org.mirza.dto.PostResponseDto;
import org.mirza.entity.Post;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import spark.Request;


import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.*;
import java.util.function.Predicate;

import static spark.Spark.*;

@Slf4j
public class Main {

    static Map<Integer, Post> database = new TreeMap<>();

    public static void main(String[] args) {

        Gson gson = new Gson();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

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

            List<Post> posts = database.values().stream()
                    .filter(Predicate.not(Post::isDeleted))
                    .toList();

            List<PostResponseDto> postResponseDtos = modelMapper.map(posts, new TypeToken<List<PostResponseDto>>(){}.getType());

            BaseResponse<List<PostResponseDto>> successGetAllPosts = BaseResponse.generateSuccessResponse("Success get all posts", postResponseDtos);
            return gson.toJson(successGetAllPosts);
        });

        // get post by Id
        get("/:id", (req, res) -> {
            Integer id  = Integer.parseInt(req.params(":id"));

            Post post = findPostbyId(id);

            PostResponseDto postResponseDto = modelMapper.map(post, PostResponseDto.class);

            BaseResponse<PostResponseDto> response = BaseResponse.generateSuccessResponse("Success get post", postResponseDto);

            return gson.toJson(response);
        });

        // create post
        post("/", (req, res) -> {

            Integer id = getLatestId();

            Post post = parseAndValidatePostRequest(req, gson, validator);

            post.setId(id);

            database.computeIfAbsent(id, k -> post);

            PostResponseDto postResponseDto = modelMapper.map(post, PostResponseDto.class);

            log.info("Suucess update post with id : {}", id);

            res.status(HttpStatus.CREATED_201);

            BaseResponse<PostResponseDto> successCreateAPost = BaseResponse.generateResponse(HttpStatus.CREATED_201, "Success create a post", postResponseDto);
            return gson.toJson(successCreateAPost);
        });

        // update post
        put("/:id", (req, res) -> {
            Integer id  = Integer.parseInt(req.params(":id"));

            Post post = findPostbyId(id);

            Post newPost = parseAndValidatePostRequest(req, gson, validator);

            post.setTitle(newPost.getTitle());
            post.setContent(newPost.getContent());

            database.computeIfPresent(post.getId(), (key, value) -> post);

            PostResponseDto postResponseDto = modelMapper.map(post, PostResponseDto.class);

            res.status(HttpStatus.ACCEPTED_202);

            BaseResponse<PostResponseDto> successUpdateAPost = BaseResponse.generateResponse(HttpStatus.ACCEPTED_202, "Success update a post", postResponseDto);
            return gson.toJson(successUpdateAPost);
        });

        // delete post
        delete("/:id", (req, res) -> {

            Integer id  = Integer.parseInt(req.params(":id"));
            Post post = findPostbyId(id);

            post.setDeleted(true);

            database.computeIfPresent(post.getId(), (key, value) -> post);

            PostResponseDto postResponseDto = modelMapper.map(post, PostResponseDto.class);

            res.status(HttpStatus.OK_200);

            BaseResponse<PostResponseDto> successDeleteAPost = BaseResponse.generateResponse(HttpStatus.OK_200, "Success delete a post", postResponseDto);
            return gson.toJson(successDeleteAPost);
        });

    }

    private static Post parseAndValidatePostRequest(Request req, Gson gson, Validator validator) {
        Post post = gson.fromJson(req.body(), Post.class);
        validateObject(validator, post);
        return post;
    }

    private static Post findPostbyId(Integer id) {
        log.info("Find post with id = [{}]", id);

        return Optional.ofNullable(database.get(id))
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    private static void validateObject(Validator validator, Object object) {
        // Validate the object
        Set<ConstraintViolation<Object>> violations = validator.validate(object);

        // If violations are found, log them and throw an exception with the aggregated message
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
            violations.forEach(violation -> {
                String message = violation.getMessage();
                errorMessage.append(message).append("; ");
            });

            // Throw a RuntimeException with all the violation messages
            throw new RuntimeException(errorMessage.toString());
        }

        // Log if there are no violations (DTO is valid)
        log.info("DTO is valid.");
    }

    private static Integer getLatestId() {
        TreeMap<Integer, Post> data = (TreeMap<Integer, Post>) database;
        if (!data.isEmpty())
            return data.lastKey() + 1;

        return 1;
    }
}
