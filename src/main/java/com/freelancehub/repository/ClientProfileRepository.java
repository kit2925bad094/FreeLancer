package com.freelancehub.repository;

import com.freelancehub.model.ClientProfile;
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
public class ClientProfileRepository {

    private final JdbcTemplate jdbcTemplate;

    public ClientProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ClientProfile> clientRowMapper = (rs, rowNum) -> {
        ClientProfile cp = new ClientProfile();
        cp.setId(rs.getLong("id"));
        cp.setUserId(rs.getLong("user_id"));
        cp.setCompanyName(rs.getString("company_name"));
        cp.setCompanyDescription(rs.getString("company_description"));
        cp.setWebsite(rs.getString("website"));
        cp.setLocation(rs.getString("location"));
        cp.setCreatedAt(rs.getTimestamp("created_at"));
        cp.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            cp.setName(rs.getString("name"));
            cp.setEmail(rs.getString("email"));
            cp.setProfileImage(rs.getString("profile_image"));
            cp.setStatus(rs.getString("status"));
        } catch (Exception ignored) {}

        return cp;
    };

    public Optional<ClientProfile> findById(Long id) {
        String sql = """
            SELECT cp.*, u.name, u.email, u.profile_image, u.status
            FROM client_profiles cp
            JOIN users u ON cp.user_id = u.id
            WHERE cp.id = ?
        """;
        try {
            ClientProfile profile = jdbcTemplate.queryForObject(sql, clientRowMapper, id);
            return Optional.ofNullable(profile);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<ClientProfile> findByUserId(Long userId) {
        String sql = """
            SELECT cp.*, u.name, u.email, u.profile_image, u.status
            FROM client_profiles cp
            JOIN users u ON cp.user_id = u.id
            WHERE cp.user_id = ?
        """;
        try {
            ClientProfile profile = jdbcTemplate.queryForObject(sql, clientRowMapper, userId);
            return Optional.ofNullable(profile);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Long save(ClientProfile profile) {
        String sql = "INSERT INTO client_profiles (user_id, company_name, company_description, website, location) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, profile.getUserId());
            ps.setString(2, profile.getCompanyName());
            ps.setString(3, profile.getCompanyDescription());
            ps.setString(4, profile.getWebsite());
            ps.setString(5, profile.getLocation());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int update(ClientProfile profile) {
        String sql = "UPDATE client_profiles SET company_name = ?, company_description = ?, website = ?, location = ? WHERE id = ?";
        return jdbcTemplate.update(sql, profile.getCompanyName(), profile.getCompanyDescription(), profile.getWebsite(), profile.getLocation(), profile.getId());
    }

    public ArrayList<ClientProfile> findAll() {
        String sql = """
            SELECT cp.*, u.name, u.email, u.profile_image, u.status
            FROM client_profiles cp
            JOIN users u ON cp.user_id = u.id
            ORDER BY cp.created_at DESC
        """;
        List<ClientProfile> list = jdbcTemplate.query(sql, clientRowMapper);
        return new ArrayList<>(list);
    }

    public int countAll() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM client_profiles", Integer.class);
        return count != null ? count : 0;
    }
}
