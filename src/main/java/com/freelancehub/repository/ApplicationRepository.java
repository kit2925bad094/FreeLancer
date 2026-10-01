package com.freelancehub.repository;

import com.freelancehub.model.ProjectApplication;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
public class ApplicationRepository {

    private final JdbcTemplate jdbcTemplate;

    public ApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ProjectApplication> appRowMapper = (rs, rowNum) -> {
        ProjectApplication app = new ProjectApplication();
        app.setId(rs.getLong("id"));
        app.setProjectId(rs.getLong("project_id"));
        app.setFreelancerId(rs.getLong("freelancer_id"));
        app.setProposal(rs.getString("proposal"));
        app.setProposedBudget(rs.getBigDecimal("proposed_budget"));
        app.setEstimatedDays(rs.getInt("estimated_days"));
        app.setStatus(rs.getString("status"));
        app.setAppliedAt(rs.getTimestamp("applied_at"));

        try {
            app.setProjectTitle(rs.getString("project_title"));
            app.setFreelancerName(rs.getString("freelancer_name"));
            app.setFreelancerHeadline(rs.getString("freelancer_headline"));
            app.setFreelancerProfileImage(rs.getString("freelancer_profile_image"));
            app.setFreelancerRating(rs.getBigDecimal("freelancer_rating"));
        } catch (Exception ignored) {}

        return app;
    };

    public Optional<ProjectApplication> findById(Long id) {
        String sql = """
            SELECT pa.*, p.title AS project_title, u.name AS freelancer_name,
                   fp.headline AS freelancer_headline, u.profile_image AS freelancer_profile_image,
                   fp.rating AS freelancer_rating
            FROM project_applications pa
            JOIN projects p ON pa.project_id = p.id
            JOIN freelancer_profiles fp ON pa.freelancer_id = fp.id
            JOIN users u ON fp.user_id = u.id
            WHERE pa.id = ?
        """;
        try {
            ProjectApplication app = jdbcTemplate.queryForObject(sql, appRowMapper, id);
            return Optional.ofNullable(app);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<ProjectApplication> findByProjectAndFreelancer(Long projectId, Long freelancerId) {
        String sql = """
            SELECT pa.*, p.title AS project_title, u.name AS freelancer_name,
                   fp.headline AS freelancer_headline, u.profile_image AS freelancer_profile_image,
                   fp.rating AS freelancer_rating
            FROM project_applications pa
            JOIN projects p ON pa.project_id = p.id
            JOIN freelancer_profiles fp ON pa.freelancer_id = fp.id
            JOIN users u ON fp.user_id = u.id
            WHERE pa.project_id = ? AND pa.freelancer_id = ?
        """;
        try {
            ProjectApplication app = jdbcTemplate.queryForObject(sql, appRowMapper, projectId, freelancerId);
            return Optional.ofNullable(app);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public HashSet<Long> findApplicantFreelancerIdsByProjectId(Long projectId) {
        String sql = "SELECT freelancer_id FROM project_applications WHERE project_id = ?";
        List<Long> ids = jdbcTemplate.queryForList(sql, Long.class, projectId);
        return new HashSet<>(ids);
    }

    public Long save(ProjectApplication app) {
        String sql = """
            INSERT INTO project_applications (project_id, freelancer_id, proposal, proposed_budget, estimated_days, status)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, app.getProjectId());
            ps.setLong(2, app.getFreelancerId());
            ps.setString(3, app.getProposal());
            ps.setBigDecimal(4, app.getProposedBudget());
            ps.setInt(5, app.getEstimatedDays());
            ps.setString(6, app.getStatus() != null ? app.getStatus() : "PENDING");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int updateStatus(Long id, String status) {
        String sql = "UPDATE project_applications SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }

    public ArrayList<ProjectApplication> findByProjectId(Long projectId) {
        String sql = """
            SELECT pa.*, p.title AS project_title, u.name AS freelancer_name,
                   fp.headline AS freelancer_headline, u.profile_image AS freelancer_profile_image,
                   fp.rating AS freelancer_rating
            FROM project_applications pa
            JOIN projects p ON pa.project_id = p.id
            JOIN freelancer_profiles fp ON pa.freelancer_id = fp.id
            JOIN users u ON fp.user_id = u.id
            WHERE pa.project_id = ?
            ORDER BY pa.applied_at DESC
        """;
        List<ProjectApplication> list = jdbcTemplate.query(sql, appRowMapper, projectId);
        return new ArrayList<>(list);
    }

    public ArrayList<ProjectApplication> findByFreelancerId(Long freelancerId) {
        String sql = """
            SELECT pa.*, p.title AS project_title, u.name AS freelancer_name,
                   fp.headline AS freelancer_headline, u.profile_image AS freelancer_profile_image,
                   fp.rating AS freelancer_rating
            FROM project_applications pa
            JOIN projects p ON pa.project_id = p.id
            JOIN freelancer_profiles fp ON pa.freelancer_id = fp.id
            JOIN users u ON fp.user_id = u.id
            WHERE pa.freelancer_id = ?
            ORDER BY pa.applied_at DESC
        """;
        List<ProjectApplication> list = jdbcTemplate.query(sql, appRowMapper, freelancerId);
        return new ArrayList<>(list);
    }

    public int countByFreelancerId(Long freelancerId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM project_applications WHERE freelancer_id = ?", Integer.class, freelancerId);
        return count != null ? count : 0;
    }

    public int countTotal() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM project_applications", Integer.class);
        return count != null ? count : 0;
    }
}
