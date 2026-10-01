package com.freelancehub.repository;

import com.freelancehub.model.Notification;
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
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Notification> notifRowMapper = (rs, rowNum) -> {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setIsRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    };

    public Long save(Notification notif) {
        String sql = "INSERT INTO notifications (user_id, title, message, type, is_read) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, notif.getUserId());
            ps.setString(2, notif.getTitle());
            ps.setString(3, notif.getMessage());
            ps.setString(4, notif.getType());
            ps.setBoolean(5, notif.getIsRead() != null ? notif.getIsRead() : false);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public ArrayList<Notification> findByUserId(Long userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        List<Notification> list = jdbcTemplate.query(sql, notifRowMapper, userId);
        return new ArrayList<>(list);
    }

    public int markAsRead(Long notifId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        return jdbcTemplate.update(sql, notifId);
    }

    public int markAllAsReadForUser(Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        return jdbcTemplate.update(sql, userId);
    }

    public int countUnreadByUserId(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE", Integer.class, userId);
        return count != null ? count : 0;
    }
}
