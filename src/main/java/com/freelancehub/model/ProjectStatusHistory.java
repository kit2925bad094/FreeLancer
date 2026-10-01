package com.freelancehub.model;

import java.sql.Timestamp;

public class ProjectStatusHistory {
    private Long id;
    private Long projectId;
    private String status;
    private Long changedBy;
    private Timestamp changedAt;

    private String changedByName;

    public ProjectStatusHistory() {}

    public ProjectStatusHistory(Long id, Long projectId, String status, Long changedBy, Timestamp changedAt) {
        this.id = id;
        this.projectId = projectId;
        this.status = status;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getChangedBy() { return changedBy; }
    public void setChangedBy(Long changedBy) { this.changedBy = changedBy; }

    public Timestamp getChangedAt() { return changedAt; }
    public void setChangedAt(Timestamp changedAt) { this.changedAt = changedAt; }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }
}
