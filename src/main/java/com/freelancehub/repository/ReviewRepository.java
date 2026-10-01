package com.freelancehub.repository;

import com.freelancehub.model.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> {
        Review r = new Review();
        r.setId(rs.getLong("id"));
        r.setProjectId(rs.getLong("project_id"));
        r.setReviewerId(rs.getLong("reviewer_id"));
        r.setRevieweeId(rs.getLong("reviewee_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        r.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            r.setReviewerName(rs.getString("reviewer_name"));
            r.setReviewerRole(rs.getString("reviewer_role"));
            r.setRevieweeName(rs.getString("reviewee_name"));
            r.setProjectTitle(rs.getString("project_title"));
        } catch (Exception ignored) {}

        return r;
    };

    public Long save(Review review) {
        String sql = "INSERT INTO reviews (project_id, reviewer_id, reviewee_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, review.getProjectId());
            ps.setLong(2, review.getReviewerId());
            ps.setLong(3, review.getRevieweeId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComment());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public ArrayList<Review> findByProjectId(Long projectId) {
        String sql = """
            SELECT r.*, ru.name AS reviewer_name, ru.role AS reviewer_role, ee.name AS reviewee_name, p.title AS project_title
            FROM reviews r
            JOIN users ru ON r.reviewer_id = ru.id
            JOIN users ee ON r.reviewee_id = ee.id
            JOIN projects p ON r.project_id = p.id
            WHERE r.project_id = ?
            ORDER BY r.created_at DESC
        """;
        List<Review> list = jdbcTemplate.query(sql, reviewRowMapper, projectId);
        return new ArrayList<>(list);
    }

    public ArrayList<Review> findByRevieweeId(Long revieweeId) {
        String sql = """
            SELECT r.*, ru.name AS reviewer_name, ru.role AS reviewer_role, ee.name AS reviewee_name, p.title AS project_title
            FROM reviews r
            JOIN users ru ON r.reviewer_id = ru.id
            JOIN users ee ON r.reviewee_id = ee.id
            JOIN projects p ON r.project_id = p.id
            WHERE r.reviewee_id = ?
            ORDER BY r.created_at DESC
        """;
        List<Review> list = jdbcTemplate.query(sql, reviewRowMapper, revieweeId);
        return new ArrayList<>(list);
    }

    public double calculateAverageRatingForUser(Long userId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE reviewee_id = ?";
        Double avg = jdbcTemplate.queryForObject(sql, Double.class, userId);
        return avg != null ? Math.round(avg * 100.0) / 100.0 : 5.0;
    }
}
