package com.freelancehub.repository;

import com.freelancehub.model.Message;
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
public class MessageRepository {

    private final JdbcTemplate jdbcTemplate;

    public MessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Message> messageRowMapper = (rs, rowNum) -> {
        Message m = new Message();
        m.setId(rs.getLong("id"));
        m.setSenderId(rs.getLong("sender_id"));
        m.setReceiverId(rs.getLong("receiver_id"));
        m.setProjectId(rs.getLong("project_id"));
        m.setMessage(rs.getString("message"));
        m.setSentAt(rs.getTimestamp("sent_at"));
        m.setIsRead(rs.getBoolean("is_read"));

        try {
            m.setSenderName(rs.getString("sender_name"));
            m.setSenderRole(rs.getString("sender_role"));
            m.setReceiverName(rs.getString("receiver_name"));
        } catch (Exception ignored) {}

        return m;
    };

    public Long save(Message msg) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, project_id, message, is_read) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, msg.getSenderId());
            ps.setLong(2, msg.getReceiverId());
            ps.setLong(3, msg.getProjectId());
            ps.setString(4, msg.getMessage());
            ps.setBoolean(5, msg.getIsRead() != null ? msg.getIsRead() : false);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public ArrayList<Message> findByProjectId(Long projectId) {
        String sql = """
            SELECT m.*, su.name AS sender_name, su.role AS sender_role, ru.name AS receiver_name
            FROM messages m
            JOIN users su ON m.sender_id = su.id
            JOIN users ru ON m.receiver_id = ru.id
            WHERE m.project_id = ?
            ORDER BY m.sent_at ASC
        """;
        List<Message> list = jdbcTemplate.query(sql, messageRowMapper, projectId);
        return new ArrayList<>(list);
    }

    public ArrayList<Message> findByUserId(Long userId) {
        String sql = """
            SELECT m.*, su.name AS sender_name, su.role AS sender_role, ru.name AS receiver_name
            FROM messages m
            JOIN users su ON m.sender_id = su.id
            JOIN users ru ON m.receiver_id = ru.id
            WHERE m.sender_id = ? OR m.receiver_id = ?
            ORDER BY m.sent_at DESC
        """;
        List<Message> list = jdbcTemplate.query(sql, messageRowMapper, userId, userId);
        return new ArrayList<>(list);
    }

    public int markAsRead(Long messageId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE id = ?";
        return jdbcTemplate.update(sql, messageId);
    }
}
