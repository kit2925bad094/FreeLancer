package com.freelancehub.model;

import java.sql.Timestamp;

public class ProjectAssignment {
    private Long id;
    private Long projectId;
    private Long freelancerId;
    private Timestamp assignedAt;

    private String projectTitle;
    private String freelancerName;

    public ProjectAssignment() {}

    public ProjectAssignment(Long id, Long projectId, Long freelancerId, Timestamp assignedAt) {
        this.id = id;
        this.projectId = projectId;
        this.freelancerId = freelancerId;
        this.assignedAt = assignedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public Timestamp getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Timestamp assignedAt) { this.assignedAt = assignedAt; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getFreelancerName() { return freelancerName; }
    public void setFreelancerName(String freelancerName) { this.freelancerName = freelancerName; }
}
