package com.freelancehub.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Payment {
    private Long id;
    private Long projectId;
    private Long clientId;
    private Long freelancerId;
    private BigDecimal amount;
    private String status; // PENDING, PARTIAL, PAID
    private Timestamp paymentDate;

    private String projectTitle;
    private String clientName;
    private String freelancerName;

    public Payment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getFreelancerName() { return freelancerName; }
    public void setFreelancerName(String freelancerName) { this.freelancerName = freelancerName; }
}
