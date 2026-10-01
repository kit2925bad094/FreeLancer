package com.freelancehub.repository;

import com.freelancehub.model.ProjectAssignment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class ProjectAssignmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProjectAssignmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ProjectAssignment> rowMapper = (rs, rowNum) -> {
        ProjectAssignment pa = new ProjectAssignment();
        pa.setId(rs.getLong("id"));
        pa.setProjectId(rs.getLong("project_id"));
        pa.setFreelancerId(rs.getLong("freelancer_id"));
        pa.setAssignedAt(rs.getTimestamp("assigned_at"));
        return pa;
    };

    public Optional<ProjectAssignment> findByProjectId(Long projectId) {
        String sql = "SELECT * FROM project_assignments WHERE project_id = ?";
        try {
            ProjectAssignment pa = jdbcTemplate.queryForObject(sql, rowMapper, projectId);
            return Optional.ofNullable(pa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Long save(Long projectId, Long freelancerId) {
        String sql = "INSERT INTO project_assignments (project_id, freelancer_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE freelancer_id = VALUES(freelancer_id)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, projectId);
            ps.setLong(2, freelancerId);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }
}
