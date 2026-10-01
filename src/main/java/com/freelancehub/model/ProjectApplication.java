package com.freelancehub.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;

public class ProjectApplication {
    private Long id;
    private Long projectId;
    private Long freelancerId;
    private String proposal;
    private BigDecimal proposedBudget;
    private Integer estimatedDays;
    private String status; // PENDING, ACCEPTED, REJECTED, WITHDRAWN
    private Timestamp appliedAt;

    // Joined fields for rich display
    private String projectTitle;
    private String freelancerName;
    private String freelancerHeadline;
    private String freelancerProfileImage;
    private BigDecimal freelancerRating;
    private ArrayList<Skill> freelancerSkills = new ArrayList<>();
    private Integer skillMatchPercentage;

    public ProjectApplication() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public String getProposal() { return proposal; }
    public void setProposal(String proposal) { this.proposal = proposal; }

    public BigDecimal getProposedBudget() { return proposedBudget; }
    public void setProposedBudget(BigDecimal proposedBudget) { this.proposedBudget = proposedBudget; }

    public Integer getEstimatedDays() { return estimatedDays; }
    public void setEstimatedDays(Integer estimatedDays) { this.estimatedDays = estimatedDays; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Timestamp appliedAt) { this.appliedAt = appliedAt; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getFreelancerName() { return freelancerName; }
    public void setFreelancerName(String freelancerName) { this.freelancerName = freelancerName; }

    public String getFreelancerHeadline() { return freelancerHeadline; }
    public void setFreelancerHeadline(String freelancerHeadline) { this.freelancerHeadline = freelancerHeadline; }

    public String getFreelancerProfileImage() { return freelancerProfileImage; }
    public void setFreelancerProfileImage(String freelancerProfileImage) { this.freelancerProfileImage = freelancerProfileImage; }

    public BigDecimal getFreelancerRating() { return freelancerRating; }
    public void setFreelancerRating(BigDecimal freelancerRating) { this.freelancerRating = freelancerRating; }

    public ArrayList<Skill> getFreelancerSkills() { return freelancerSkills; }
    public void setFreelancerSkills(ArrayList<Skill> freelancerSkills) { this.freelancerSkills = freelancerSkills; }

    public Integer getSkillMatchPercentage() { return skillMatchPercentage; }
    public void setSkillMatchPercentage(Integer skillMatchPercentage) { this.skillMatchPercentage = skillMatchPercentage; }
}
