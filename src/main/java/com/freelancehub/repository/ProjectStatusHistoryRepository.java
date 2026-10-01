package com.freelancehub.repository;

import com.freelancehub.model.ProjectStatusHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProjectStatusHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProjectStatusHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ProjectStatusHistory> rowMapper = (rs, rowNum) -> {
        ProjectStatusHistory h = new ProjectStatusHistory();
        h.setId(rs.getLong("id"));
        h.setProjectId(rs.getLong("project_id"));
        h.setStatus(rs.getString("status"));
        h.setChangedBy(rs.getLong("changed_by"));
        h.setChangedAt(rs.getTimestamp("changed_at"));
        try {
            h.setChangedByName(rs.getString("changed_by_name"));
        } catch (Exception ignored) {}
        return h;
    };

    public void save(Long projectId, String status, Long changedBy) {
        String sql = "INSERT INTO project_status_history (project_id, status, changed_by) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, projectId, status, changedBy);
    }

    public ArrayList<ProjectStatusHistory> findByProjectId(Long projectId) {
        String sql = """
            SELECT psh.*, u.name AS changed_by_name
            FROM project_status_history psh
            LEFT JOIN users u ON psh.changed_by = u.id
            WHERE psh.project_id = ?
            ORDER BY psh.changed_at ASC
        """;
        List<ProjectStatusHistory> list = jdbcTemplate.query(sql, rowMapper, projectId);
        return new ArrayList<>(list);
    }
}
