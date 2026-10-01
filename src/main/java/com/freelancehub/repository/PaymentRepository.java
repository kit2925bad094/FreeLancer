package com.freelancehub.repository;

import com.freelancehub.model.Payment;
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

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Payment> paymentRowMapper = (rs, rowNum) -> {
        Payment p = new Payment();
        p.setId(rs.getLong("id"));
        p.setProjectId(rs.getLong("project_id"));
        p.setClientId(rs.getLong("client_id"));
        p.setFreelancerId(rs.getLong("freelancer_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setStatus(rs.getString("status"));
        p.setPaymentDate(rs.getTimestamp("payment_date"));

        try {
            p.setProjectTitle(rs.getString("project_title"));
            p.setClientName(rs.getString("company_name"));
            p.setFreelancerName(rs.getString("freelancer_name"));
        } catch (Exception ignored) {}

        return p;
    };

    public Long save(Payment p) {
        String sql = "INSERT INTO payments (project_id, client_id, freelancer_id, amount, status) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, p.getProjectId());
            ps.setLong(2, p.getClientId());
            ps.setLong(3, p.getFreelancerId());
            ps.setBigDecimal(4, p.getAmount());
            ps.setString(5, p.getStatus() != null ? p.getStatus() : "PENDING");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public int updateStatus(Long id, String status) {
        String sql = "UPDATE payments SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }

    public ArrayList<Payment> findByFreelancerId(Long freelancerId) {
        String sql = """
            SELECT pm.*, p.title AS project_title, cp.company_name, u.name AS freelancer_name
            FROM payments pm
            JOIN projects p ON pm.project_id = p.id
            JOIN client_profiles cp ON pm.client_id = cp.id
            JOIN freelancer_profiles fp ON pm.freelancer_id = fp.id
            JOIN users u ON fp.user_id = u.id
            WHERE pm.freelancer_id = ?
            ORDER BY pm.payment_date DESC
        """;
        List<Payment> list = jdbcTemplate.query(sql, paymentRowMapper, freelancerId);
        return new ArrayList<>(list);
    }

    public ArrayList<Payment> findByClientId(Long clientId) {
        String sql = """
            SELECT pm.*, p.title AS project_title, cp.company_name, u.name AS freelancer_name
            FROM payments pm
            JOIN projects p ON pm.project_id = p.id
            JOIN client_profiles cp ON pm.client_id = cp.id
            JOIN freelancer_profiles fp ON pm.freelancer_id = fp.id
            JOIN users u ON fp.user_id = u.id
            WHERE pm.client_id = ?
            ORDER BY pm.payment_date DESC
        """;
        List<Payment> list = jdbcTemplate.query(sql, paymentRowMapper, clientId);
        return new ArrayList<>(list);
    }

    public BigDecimal sumEarningsByFreelancer(Long freelancerId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE freelancer_id = ? AND status = 'PAID'";
        BigDecimal sum = jdbcTemplate.queryForObject(sql, BigDecimal.class, freelancerId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public BigDecimal sumSpendingByClient(Long clientId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE client_id = ? AND status = 'PAID'";
        BigDecimal sum = jdbcTemplate.queryForObject(sql, BigDecimal.class, clientId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public BigDecimal sumTotalPaidPlatform() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'PAID'";
        BigDecimal sum = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
