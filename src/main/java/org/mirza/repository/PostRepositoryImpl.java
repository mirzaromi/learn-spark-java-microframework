package org.mirza.repository;

import org.mirza.dto.pagination.PaginationRequestDto;
import org.mirza.entity.Post;
import org.mirza.exception.DatabaseException;
import org.mirza.exception.NotFoundException;
import org.mirza.util.DatabaseUtil;
import spark.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mirza.constant.GlobalConstant.DATABASE_ERROR;

public class PostRepositoryImpl implements PostRepository {
    @Override
    public List<Post> getAllPost(Response res, PaginationRequestDto paginationRequest) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            List<Post> tempPosts = new ArrayList<>();

            String query = "SELECT * FROM posts WHERE is_deleted = false ORDER BY ? LIMIT ? OFFSET ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, paginationRequest.getOrderBy());
                statement.setInt(2, paginationRequest.getSize());
                statement.setInt(3, paginationRequest.getOffset());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    Integer id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    String content = resultSet.getString("content");
                    boolean isDeleted = resultSet.getBoolean("is_deleted");

                    tempPosts.add(new Post(id, title, content, isDeleted));
                }
            }

            return tempPosts;

        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            throw new DatabaseException(DATABASE_ERROR);
        }
    }

    @Override
    public Integer countAllPost(Response res) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            int totalData = 0;

            try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM posts WHERE is_deleted = false")) {
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    totalData = resultSet.getInt(1);
                }
            }

            return totalData;

        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            throw new DatabaseException(DATABASE_ERROR);
        }
    }

    @Override
    public Post findPostById(Response res, Integer requestId) {
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

    @Override
    public Boolean insertPost(Response res, Post post) {
        boolean isSuccessDBOperation = true;

        try (Connection connection = DatabaseUtil.getConnection()) {
            // create SQL Query to Insert data to DB
            String query = "INSERT INTO posts (title, content, is_deleted) VALUES (?, ?, ?)";

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setBoolean(3, post.isDeleted());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected <= 0)
                isSuccessDBOperation = false;

            connection.commit();
        } catch (Exception e) {
            rollbackCommit();

            e.printStackTrace();
            res.status(500);
            throw new DatabaseException(DATABASE_ERROR);
        }
        return isSuccessDBOperation;
    }

    @Override
    public Boolean updatePost(Response res, Post post) {
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
            connection.commit();

        } catch (Exception e) {
            rollbackCommit();

            e.printStackTrace();
            res.status(500);
            throw new DatabaseException(DATABASE_ERROR);
        }
        return isSuccessDBOperation;
    }

    private static void rollbackCommit() {
        try (Connection connection = DatabaseUtil.getConnection()) {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DatabaseException("Rollback failed: " + ex.getMessage());
        }
    }

}
