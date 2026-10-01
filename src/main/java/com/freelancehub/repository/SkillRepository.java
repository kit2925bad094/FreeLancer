package com.freelancehub.repository;

import com.freelancehub.model.Skill;
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
public class SkillRepository {

    private final JdbcTemplate jdbcTemplate;

    public SkillRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Skill> skillRowMapper = (rs, rowNum) ->
            new Skill(rs.getLong("id"), rs.getString("name"));

    public Optional<Skill> findById(Long id) {
        String sql = "SELECT * FROM skills WHERE id = ?";
        try {
            Skill skill = jdbcTemplate.queryForObject(sql, skillRowMapper, id);
            return Optional.ofNullable(skill);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Skill> findByName(String name) {
        String sql = "SELECT * FROM skills WHERE LOWER(name) = LOWER(?)";
        try {
            Skill skill = jdbcTemplate.queryForObject(sql, skillRowMapper, name.trim());
            return Optional.ofNullable(skill);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Long save(Skill skill) {
        String sql = "INSERT INTO skills (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, skill.getName().trim());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public ArrayList<Skill> findAll() {
        String sql = "SELECT * FROM skills ORDER BY name ASC";
        List<Skill> list = jdbcTemplate.query(sql, skillRowMapper);
        return new ArrayList<>(list);
    }

    public ArrayList<Skill> findSkillsByFreelancerId(Long freelancerId) {
        String sql = """
            SELECT s.* FROM skills s
            JOIN freelancer_skills fs ON s.id = fs.skill_id
            WHERE fs.freelancer_id = ?
            ORDER BY s.name ASC
        """;
        List<Skill> list = jdbcTemplate.query(sql, skillRowMapper, freelancerId);
        return new ArrayList<>(list);
    }

    public HashSet<String> findSkillNamesByFreelancerId(Long freelancerId) {
        ArrayList<Skill> skills = findSkillsByFreelancerId(freelancerId);
        HashSet<String> set = new HashSet<>();
        for (Skill s : skills) {
            set.add(s.getName().toLowerCase());
        }
        return set;
    }

    public void addSkillToFreelancer(Long freelancerId, Long skillId) {
        String sql = "INSERT IGNORE INTO freelancer_skills (freelancer_id, skill_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, freelancerId, skillId);
    }

    public void removeSkillsFromFreelancer(Long freelancerId) {
        String sql = "DELETE FROM freelancer_skills WHERE freelancer_id = ?";
        jdbcTemplate.update(sql, freelancerId);
    }

    public ArrayList<Skill> findSkillsByProjectId(Long projectId) {
        String sql = """
            SELECT s.* FROM skills s
            JOIN project_skills ps ON s.id = ps.skill_id
            WHERE ps.project_id = ?
            ORDER BY s.name ASC
        """;
        List<Skill> list = jdbcTemplate.query(sql, skillRowMapper, projectId);
        return new ArrayList<>(list);
    }

    public HashSet<String> findSkillNamesByProjectId(Long projectId) {
        ArrayList<Skill> skills = findSkillsByProjectId(projectId);
        HashSet<String> set = new HashSet<>();
        for (Skill s : skills) {
            set.add(s.getName().toLowerCase());
        }
        return set;
    }

    public void addSkillToProject(Long projectId, Long skillId) {
        String sql = "INSERT IGNORE INTO project_skills (project_id, skill_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, projectId, skillId);
    }

    public void removeSkillsFromProject(Long projectId) {
        String sql = "DELETE FROM project_skills WHERE project_id = ?";
        jdbcTemplate.update(sql, projectId);
    }
}
