package com.freelancehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class StatusUpdateRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "OPEN|ASSIGNED|IN_PROGRESS|UNDER_REVIEW|COMPLETED|CANCELLED", message = "Invalid project status")
    private String status;

    @NotNull(message = "Changed by user ID is required")
    private Long changedBy;

    public StatusUpdateRequest() {}

    public StatusUpdateRequest(String status, Long changedBy) {
        this.status = status;
        this.changedBy = changedBy;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getChangedBy() { return changedBy; }
    public void setChangedBy(Long changedBy) { this.changedBy = changedBy; }
}
