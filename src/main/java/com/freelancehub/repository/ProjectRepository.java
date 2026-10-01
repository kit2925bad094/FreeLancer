package com.freelancehub.repository;

import com.freelancehub.model.Project;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProjectRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Project> projectRowMapper = (rs, rowNum) -> {
        Project p = new Project();
        p.setId(rs.getLong("id"));
        p.setClientId(rs.getLong("client_id"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setCategory(rs.getString("category"));
        p.setBudgetMin(rs.getBigDecimal("budget_min"));
        p.setBudgetMax(rs.getBigDecimal("budget_max"));
        p.setDeadline(rs.getDate("deadline"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            p.setClientCompanyName(rs.getString("company_name"));
            p.setClientLocation(rs.getString("client_location"));
            p.setClientName(rs.getString("client_user_name"));
        } catch (Exception ignored) {}

        try {
            p.setApplicantCount(rs.getInt("applicant_count"));
        } catch (Exception ignored) {}

        try {
            long assignedFId = rs.getLong("assigned_freelancer_id");
            if (!rs.wasNull()) {
                p.setAssignedFreelancerId(assignedFId);
                p.setAssignedFreelancerName(rs.getString("assigned_freelancer_name"));
            }
        } catch (Exception ignored) {}

        return p;
    };

    public Optional<Project> findById(Long id) {
        String sql = """
            SELECT p.*,
                   cp.company_name, cp.location AS client_location, u.name AS client_user_name,
                   (SELECT COUNT(*) FROM project_applications WHERE project_id = p.id) AS applicant_count,
                   pa.freelancer_id AS assigned_freelancer_id, fu.name AS assigned_freelancer_name
            FROM projects p
            JOIN client_profiles cp ON p.client_id = cp.id
            JOIN users u ON cp.user_id = u.id
            LEFT JOIN project_assignments pa ON p.id = pa.project_id
            LEFT JOIN freelancer_profiles fp ON pa.freelancer_id = fp.id
            LEFT JOIN users fu ON fp.user_id = fu.id
            WHERE p.id = ?
        """;
        try {
            Project project = jdbcTemplate.queryForObject(sql, projectRowMapper, id);
            return Optional.ofNullable(project);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Long save(Project project) {
        String sql = """
            INSERT INTO projects (client_id, title, description, category, budget_min, budget_max, deadline, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, project.getClientId());
            ps.setString(2, project.getTitle());
            ps.setString(3, project.getDescription());
            ps.setString(4, project.getCategory());
            ps.setBigDecimal(5, project.getBudgetMin());
            ps.setBigDecimal(6, project.getBudgetMax());
            ps.setDate(7, project.getDeadline());
            ps.setString(8, project.getStatus() != null ? project.getStatus() : "OPEN");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int update(Project project) {
        String sql = """
            UPDATE projects
            SET title = ?, description = ?, category = ?, budget_min = ?, budget_max = ?, deadline = ?, status = ?
            WHERE id = ?
        """;
        return jdbcTemplate.update(sql,
                project.getTitle(), project.getDescription(), project.getCategory(),
                project.getBudgetMin(), project.getBudgetMax(), project.getDeadline(),
                project.getStatus(), project.getId());
    }

    public int updateStatus(Long projectId, String status) {
        String sql = "UPDATE projects SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, projectId);
    }

    public int delete(Long id) {
        String sql = "DELETE FROM projects WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public ArrayList<Project> findAll() {
        String sql = """
            SELECT p.*,
                   cp.company_name, cp.location AS client_location, u.name AS client_user_name,
                   (SELECT COUNT(*) FROM project_applications WHERE project_id = p.id) AS applicant_count,
                   pa.freelancer_id AS assigned_freelancer_id, fu.name AS assigned_freelancer_name
            FROM projects p
            JOIN client_profiles cp ON p.client_id = cp.id
            JOIN users u ON cp.user_id = u.id
            LEFT JOIN project_assignments pa ON p.id = pa.project_id
            LEFT JOIN freelancer_profiles fp ON pa.freelancer_id = fp.id
            LEFT JOIN users fu ON fp.user_id = fu.id
            ORDER BY p.created_at DESC
        """;
        List<Project> list = jdbcTemplate.query(sql, projectRowMapper);
        return new ArrayList<>(list);
    }

    public ArrayList<Project> searchAndFilter(String query, String category, String skill,
                                             BigDecimal minBudget, BigDecimal maxBudget, String status, String sortBy) {
        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT p.*,
                   cp.company_name, cp.location AS client_location, u.name AS client_user_name,
                   (SELECT COUNT(*) FROM project_applications WHERE project_id = p.id) AS applicant_count,
                   pa.freelancer_id AS assigned_freelancer_id, fu.name AS assigned_freelancer_name
            FROM projects p
            JOIN client_profiles cp ON p.client_id = cp.id
            JOIN users u ON cp.user_id = u.id
            LEFT JOIN project_assignments pa ON p.id = pa.project_id
            LEFT JOIN freelancer_profiles fp ON pa.freelancer_id = fp.id
            LEFT JOIN users fu ON fp.user_id = fu.id
            LEFT JOIN project_skills ps ON p.id = ps.project_id
            LEFT JOIN skills s ON ps.skill_id = s.id
            WHERE 1=1
        """);
        ArrayList<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(p.title) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(cp.company_name) LIKE ?)");
            String pattern = "%" + query.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            sql.append(" AND LOWER(p.category) = ?");
            params.add(category.trim().toLowerCase());
        }

        if (skill != null && !skill.trim().isEmpty()) {
            sql.append(" AND LOWER(s.name) = ?");
            params.add(skill.trim().toLowerCase());
        }

        if (minBudget != null) {
            sql.append(" AND p.budget_max >= ?");
            params.add(minBudget);
        }

        if (maxBudget != null) {
            sql.append(" AND p.budget_min <= ?");
            params.add(maxBudget);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append(" AND p.status = ?");
            params.add(status.trim().toUpperCase());
        }

        if ("budget_asc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.budget_min ASC");
        } else if ("budget_desc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.budget_max DESC");
        } else if ("deadline".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.deadline ASC");
        } else {
            sql.append(" ORDER BY p.created_at DESC");
        }

        List<Project> list = jdbcTemplate.query(sql.toString(), projectRowMapper, params.toArray());
        return new ArrayList<>(list);
    }

    public ArrayList<Project> findByClientId(Long clientId) {
        String sql = """
            SELECT p.*,
                   cp.company_name, cp.location AS client_location, u.name AS client_user_name,
                   (SELECT COUNT(*) FROM project_applications WHERE project_id = p.id) AS applicant_count,
                   pa.freelancer_id AS assigned_freelancer_id, fu.name AS assigned_freelancer_name
            FROM projects p
            JOIN client_profiles cp ON p.client_id = cp.id
            JOIN users u ON cp.user_id = u.id
            LEFT JOIN project_assignments pa ON p.id = pa.project_id
            LEFT JOIN freelancer_profiles fp ON pa.freelancer_id = fp.id
            LEFT JOIN users fu ON fp.user_id = fu.id
            WHERE p.client_id = ?
            ORDER BY p.created_at DESC
        """;
        List<Project> list = jdbcTemplate.query(sql, projectRowMapper, clientId);
        return new ArrayList<>(list);
    }

    public ArrayList<Project> findAssignedToFreelancer(Long freelancerId) {
        String sql = """
            SELECT p.*,
                   cp.company_name, cp.location AS client_location, u.name AS client_user_name,
                   (SELECT COUNT(*) FROM project_applications WHERE project_id = p.id) AS applicant_count,
                   pa.freelancer_id AS assigned_freelancer_id, fu.name AS assigned_freelancer_name
            FROM projects p
            JOIN client_profiles cp ON p.client_id = cp.id
            JOIN users u ON cp.user_id = u.id
            JOIN project_assignments pa ON p.id = pa.project_id
            JOIN freelancer_profiles fp ON pa.freelancer_id = fp.id
            JOIN users fu ON fp.user_id = fu.id
            WHERE pa.freelancer_id = ?
            ORDER BY p.updated_at DESC
        """;
        List<Project> list = jdbcTemplate.query(sql, projectRowMapper, freelancerId);
        return new ArrayList<>(list);
    }

    public int countTotal() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM projects", Integer.class);
        return count != null ? count : 0;
    }

    public int countByStatus(String status) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM projects WHERE status = ?", Integer.class, status);
        return count != null ? count : 0;
    }
}
