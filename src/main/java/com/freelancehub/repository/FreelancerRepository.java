package com.freelancehub.repository;

import com.freelancehub.model.FreelancerProfile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FreelancerRepository {

    private final JdbcTemplate jdbcTemplate;

    public FreelancerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FreelancerProfile> profileRowMapper = (rs, rowNum) -> {
        FreelancerProfile profile = new FreelancerProfile();
        profile.setId(rs.getLong("id"));
        profile.setUserId(rs.getLong("user_id"));
        profile.setHeadline(rs.getString("headline"));
        profile.setBio(rs.getString("bio"));
        profile.setEducation(rs.getString("education"));
        profile.setExperience(rs.getString("experience"));
        profile.setLocation(rs.getString("location"));
        profile.setGithubUrl(rs.getString("github_url"));
        profile.setLinkedinUrl(rs.getString("linkedin_url"));
        profile.setPortfolioUrl(rs.getString("portfolio_url"));
        profile.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        profile.setAvailability(rs.getString("availability"));
        profile.setRating(rs.getBigDecimal("rating"));
        profile.setCompletedProjects(rs.getInt("completed_projects"));
        profile.setCreatedAt(rs.getTimestamp("created_at"));
        profile.setUpdatedAt(rs.getTimestamp("updated_at"));

        // Joined user details if present in select
        try {
            profile.setName(rs.getString("name"));
            profile.setEmail(rs.getString("email"));
            profile.setProfileImage(rs.getString("profile_image"));
            profile.setStatus(rs.getString("status"));
        } catch (Exception ignored) {}

        return profile;
    };

    public Optional<FreelancerProfile> findById(Long id) {
        String sql = """
            SELECT fp.*, u.name, u.email, u.profile_image, u.status
            FROM freelancer_profiles fp
            JOIN users u ON fp.user_id = u.id
            WHERE fp.id = ?
        """;
        try {
            FreelancerProfile profile = jdbcTemplate.queryForObject(sql, profileRowMapper, id);
            return Optional.ofNullable(profile);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<FreelancerProfile> findByUserId(Long userId) {
        String sql = """
            SELECT fp.*, u.name, u.email, u.profile_image, u.status
            FROM freelancer_profiles fp
            JOIN users u ON fp.user_id = u.id
            WHERE fp.user_id = ?
        """;
        try {
            FreelancerProfile profile = jdbcTemplate.queryForObject(sql, profileRowMapper, userId);
            return Optional.ofNullable(profile);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Long save(FreelancerProfile profile) {
        String sql = """
            INSERT INTO freelancer_profiles (user_id, headline, bio, education, experience, location, github_url, linkedin_url, portfolio_url, hourly_rate, availability)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, profile.getUserId());
            ps.setString(2, profile.getHeadline());
            ps.setString(3, profile.getBio());
            ps.setString(4, profile.getEducation());
            ps.setString(5, profile.getExperience());
            ps.setString(6, profile.getLocation());
            ps.setString(7, profile.getGithubUrl());
            ps.setString(8, profile.getLinkedinUrl());
            ps.setString(9, profile.getPortfolioUrl());
            ps.setBigDecimal(10, profile.getHourlyRate());
            ps.setString(11, profile.getAvailability() != null ? profile.getAvailability() : "AVAILABLE");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int update(FreelancerProfile profile) {
        String sql = """
            UPDATE freelancer_profiles
            SET headline = ?, bio = ?, education = ?, experience = ?, location = ?,
                github_url = ?, linkedin_url = ?, portfolio_url = ?, hourly_rate = ?, availability = ?
            WHERE id = ?
        """;
        return jdbcTemplate.update(sql,
                profile.getHeadline(), profile.getBio(), profile.getEducation(), profile.getExperience(),
                profile.getLocation(), profile.getGithubUrl(), profile.getLinkedinUrl(), profile.getPortfolioUrl(),
                profile.getHourlyRate(), profile.getAvailability(), profile.getId());
    }

    public int updateRatingAndCompletedProjects(Long freelancerId, double newRating, int completedCount) {
        String sql = "UPDATE freelancer_profiles SET rating = ?, completed_projects = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newRating, completedCount, freelancerId);
    }

    public ArrayList<FreelancerProfile> findAll() {
        String sql = """
            SELECT fp.*, u.name, u.email, u.profile_image, u.status
            FROM freelancer_profiles fp
            JOIN users u ON fp.user_id = u.id
            ORDER BY fp.rating DESC, fp.completed_projects DESC
        """;
        List<FreelancerProfile> list = jdbcTemplate.query(sql, profileRowMapper);
        return new ArrayList<>(list);
    }

    public ArrayList<FreelancerProfile> searchAndFilter(String query, String skillName) {
        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT fp.*, u.name, u.email, u.profile_image, u.status
            FROM freelancer_profiles fp
            JOIN users u ON fp.user_id = u.id
            LEFT JOIN freelancer_skills fs ON fp.id = fs.freelancer_id
            LEFT JOIN skills s ON fs.skill_id = s.id
            WHERE 1=1
        """);
        ArrayList<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(u.name) LIKE ? OR LOWER(fp.headline) LIKE ? OR LOWER(fp.bio) LIKE ?)");
            String pattern = "%" + query.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (skillName != null && !skillName.trim().isEmpty()) {
            sql.append(" AND LOWER(s.name) = ?");
            params.add(skillName.trim().toLowerCase());
        }

        sql.append(" ORDER BY fp.rating DESC");
        List<FreelancerProfile> list = jdbcTemplate.query(sql.toString(), profileRowMapper, params.toArray());
        return new ArrayList<>(list);
    }

    public int countAll() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM freelancer_profiles", Integer.class);
        return count != null ? count : 0;
    }
}
