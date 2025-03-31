package org.mirza;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.mirza.dto.BaseResponse;
import org.mirza.dto.ErrorResponse;
import org.mirza.dto.pagination.PaginationDto;
import org.mirza.dto.response.PostResponseDto;
import org.mirza.entity.Post;
import org.mirza.exception.DatabaseException;
import org.mirza.exception.NotFoundException;
import org.mirza.exception.ValidationException;
import org.mirza.util.DatabaseUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import spark.Request;
import spark.Response;


import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static spark.Spark.*;

@Slf4j
public class Main {

    public static final String DATABASE_ERROR = "Database error";
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
            int page = Optional.ofNullable(req.queryParams("page")).map(Integer::parseInt).orElse(1);
            int size = Optional.ofNullable(req.queryParams("size")).map(Integer::parseInt).orElse(10);
            String orderBy = Optional.ofNullable(req.queryParams("orderBy")).map(String::trim).orElse("id");

            int offset = (page - 1) * size;
            int totalData = 0;

            List<Post> posts = new ArrayList<>();

            try (Connection connection = DatabaseUtil.getConnection()) {
                String query = "SELECT * FROM posts WHERE is_deleted = false ORDER BY ? LIMIT ? OFFSET ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, orderBy);
                    statement.setInt(2, size);
                    statement.setInt(3, offset);
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        Integer id = resultSet.getInt("id");
                        String title = resultSet.getString("title");
                        String content = resultSet.getString("content");
                        boolean isDeleted = resultSet.getBoolean("is_deleted");

                        posts.add(new Post(id, title, content, isDeleted));
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM posts WHERE is_deleted = false")) {
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        totalData = resultSet.getInt(1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                throw new DatabaseException(DATABASE_ERROR);
            }

            int totalPage = (int) Math.ceil((double) totalData / size);

            List<PostResponseDto> postResponseDtos = modelMapper.map(posts, new TypeToken<List<PostResponseDto>>(){}.getType());

            PaginationDto<List<PostResponseDto>> paginationDto = new PaginationDto<>();
            paginationDto.setPage(page);
            paginationDto.setSize(size);
            paginationDto.setTotalData(totalData);
            paginationDto.setTotalPage(totalPage);
            paginationDto.setData(postResponseDtos);


            var successGetAllPosts = BaseResponse.generateSuccessResponse("Success get all posts", paginationDto);
            return gson.toJson(successGetAllPosts);
        });

        // get post by Id
        get("/:id", (req, res) -> {
            Integer requestId  = Integer.parseInt(req.params(":id"));

            Post post = findPostById(res, requestId);

            PostResponseDto postResponseDto = modelMapper.map(post, PostResponseDto.class);

            BaseResponse<PostResponseDto> response = BaseResponse.generateSuccessResponse("Success get post", postResponseDto);

            return gson.toJson(response);
        });

        // create post
        post("/", (req, res) -> {

            Integer id = getLatestId();

            Post post = parseAndValidatePostRequest(req, gson, validator);

            Boolean isSuccessDBOperation = insertPost(res, post);

            if (Boolean.TRUE.equals(isSuccessDBOperation)) {
                PostResponseDto postResponseDto = modelMapper.map(post, PostResponseDto.class);

                log.info("Suucess create post with id : {}", id);

                res.status(HttpStatus.CREATED_201);

                BaseResponse<PostResponseDto> successCreateAPost = BaseResponse.generateResponse(HttpStatus.CREATED_201, "Success create a post", postResponseDto);
                return gson.toJson(successCreateAPost);
            }

            throw new DatabaseException("Failed to create post.");

        });

        // update post
        put("/:id", (req, res) -> {
            Integer requestId  = Integer.parseInt(req.params(":id"));

            Post post = findPostById(res, requestId);

            Post newPost = parseAndValidatePostRequest(req, gson, validator);

            post.setTitle(newPost.getTitle());
            post.setContent(newPost.getContent());

            Boolean isSuccessDBOperation = updatePost(res, post);

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
            Post post = findPostById(res, id);

            post.setDeleted(true);

            Boolean isSuccessDBOperation = updatePost(res, post);

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

    private static Boolean updatePost(Response res, Post post) {
        boolean isSuccessDBOperation = true;

        try (Connection connection = DatabaseUtil.getConnection()) {
            // create SQL Query to Insert data to DB
            String query = "UPDATE posts SET title=?, content=?, is_deleted=? WHERE id=? ";

            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, post.getTitle());
                ps.setString(2, post.getContent());
                ps.setBoolean(3, post.isDeleted());
                ps.setInt(4, post.getId());

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected <= 0)
                    isSuccessDBOperation = false;

            }
        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            throw new DatabaseException(DATABASE_ERROR);
        }
        return isSuccessDBOperation;
    }

    private static Boolean insertPost(Response res, Post post) {
        boolean isSuccessDBOperation = true;

        try (Connection connection = DatabaseUtil.getConnection()) {
            // create SQL Query to Insert data to DB
            String query = "INSERT INTO posts (title, content, is_deleted) VALUES (?, ?, ?)";

            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, post.getTitle());
                ps.setString(2, post.getContent());
                ps.setBoolean(3, post.isDeleted());

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected <= 0)
                    isSuccessDBOperation = false;

            }
        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            throw new DatabaseException(DATABASE_ERROR);
        }
        return isSuccessDBOperation;
    }

    private static Post findPostById(Response res, Integer requestId) {
        Post post = null;

        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "SELECT * FROM posts WHERE id = ? AND is_deleted = false";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, requestId);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    String content = resultSet.getString("content");
                    boolean isDeleted = resultSet.getBoolean("is_deleted");

                    post = new Post(id, title, content, isDeleted);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            throw new DatabaseException(DATABASE_ERROR);
        }

        if (Objects.isNull(post)) {
            throw new NotFoundException("Post not found");
        }

        return post;
    }

    private static Post parseAndValidatePostRequest(Request req, Gson gson, Validator validator) {
        Post post = gson.fromJson(req.body(), Post.class);
        validateObject(validator, post);
        return post;
    }

    private static Post findPostById(Integer id) {
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
            throw new ValidationException(errorMessage.toString());
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
