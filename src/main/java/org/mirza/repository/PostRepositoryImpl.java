package org.mirza.repository;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mirza.dto.pagination.PaginationRequestDto;
import org.mirza.entity.Post;
import org.mirza.exception.DatabaseException;
import org.mirza.config.DatabaseConfig;
import org.mirza.util.HibernateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.mirza.constant.GlobalConstant.DATABASE_ERROR;

public class PostRepositoryImpl implements PostRepository {
    @Override
    public List<Post> getAllPost(PaginationRequestDto paginationRequest) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY :orderBy";
            Query<Post> query = session.createQuery(hql, Post.class);
            query.setParameter("orderBy", paginationRequest.getOrderBy());
            query.setFirstResult(paginationRequest.getOffset());
            query.setMaxResults(paginationRequest.getSize());
            return query.list();
        }
    }

    @Override
    public Integer countAllPost() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(*) FROM Post WHERE isDeleted = false";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.getSingleResult().intValue();
        }
    }

    @Override
    public Optional<Post> findPostById(Integer requestId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT p FROM Post p WHERE p.id = :id and isDeleted = false";
            Query<Post> query = session.createQuery(hql, Post.class);
            query.setParameter("id", requestId);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public Boolean insertBulkPost(List<Post> posts) {
        boolean isSuccessDBOperation = true;

        try (Connection connection = DatabaseConfig.getConnection()) {
            // create SQL Query to Insert data to DB
            String query = "INSERT INTO posts (title, content, is_deleted) VALUES (?, ?, ?)";

            PreparedStatement ps = connection.prepareStatement(query);

            for (Post post : posts) {
                ps.setString(1, post.getTitle());
                ps.setString(2, post.getContent());
                ps.setBoolean(3, post.isDeleted());
                ps.addBatch();
            }

            int[] rowsAffected = ps.executeBatch();
            if (rowsAffected.length == 0)
                isSuccessDBOperation = false;

            connection.commit();
        } catch (Exception e) {
            rollbackCommit();

            e.printStackTrace();
            throw new DatabaseException(DATABASE_ERROR);
        }
        return isSuccessDBOperation;
    }

    @Override
    public void save(Post post) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            if (Objects.isNull(post.getId())) {
                session.persist(post);
            } else {
                session.merge(post);
            }
            transaction.commit();
        }
    }

    @Override
    public void saveAll(List<Post> posts) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            int batchSize = 50;
            int count = 0;

            for(Post post : posts) {
                if (Objects.isNull(post.getId())) {
                    session.persist(post);
                } else {
                    session.merge(post);
                }

                if (++count % batchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
        }
    }

    @Override
    public void saveAndFlush(Post post) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(post);
            session.flush();
            transaction.commit();
        }
    }

    private static void rollbackCommit() {
        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DatabaseException("Rollback failed: " + ex.getMessage());
        }
    }

}
