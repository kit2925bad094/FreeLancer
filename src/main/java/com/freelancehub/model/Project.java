package com.freelancehub.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Project {
    private Long id;
    private Long clientId;
    private String title;
    private String description;
    private String category;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private Date deadline;
    private String status; // OPEN, ASSIGNED, IN_PROGRESS, UNDER_REVIEW, COMPLETED, CANCELLED
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Additional metadata for presentation & aggregations
    private String clientName;
    private String clientCompanyName;
    private String clientLocation;
    private Long assignedFreelancerId;
    private String assignedFreelancerName;
    private int applicantCount;
    private ArrayList<Skill> requiredSkills = new ArrayList<>();

    public Project() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getBudgetMin() { return budgetMin; }
    public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }

    public BigDecimal getBudgetMax() { return budgetMax; }
    public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientCompanyName() { return clientCompanyName; }
    public void setClientCompanyName(String clientCompanyName) { this.clientCompanyName = clientCompanyName; }

    public String getClientLocation() { return clientLocation; }
    public void setClientLocation(String clientLocation) { this.clientLocation = clientLocation; }

    public Long getAssignedFreelancerId() { return assignedFreelancerId; }
    public void setAssignedFreelancerId(Long assignedFreelancerId) { this.assignedFreelancerId = assignedFreelancerId; }

    public String getAssignedFreelancerName() { return assignedFreelancerName; }
    public void setAssignedFreelancerName(String assignedFreelancerName) { this.assignedFreelancerName = assignedFreelancerName; }

    public int getApplicantCount() { return applicantCount; }
    public void setApplicantCount(int applicantCount) { this.applicantCount = applicantCount; }

    public ArrayList<Skill> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(ArrayList<Skill> requiredSkills) { this.requiredSkills = requiredSkills; }
}
