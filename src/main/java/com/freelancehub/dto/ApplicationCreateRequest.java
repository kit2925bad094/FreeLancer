package com.freelancehub.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ApplicationCreateRequest {
    @NotNull(message = "Freelancer ID is required")
    private Long freelancerId;

    @NotBlank(message = "Proposal is required")
    private String proposal;

    @NotNull(message = "Proposed budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Proposed budget must be greater than 0")
    private BigDecimal proposedBudget;

    @NotNull(message = "Estimated days is required")
    @Min(value = 1, message = "Estimated days must be at least 1")
    private Integer estimatedDays;

    public ApplicationCreateRequest() {}

    public ApplicationCreateRequest(Long freelancerId, String proposal, BigDecimal proposedBudget, Integer estimatedDays) {
        this.freelancerId = freelancerId;
        this.proposal = proposal;
        this.proposedBudget = proposedBudget;
        this.estimatedDays = estimatedDays;
    }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public String getProposal() { return proposal; }
    public void setProposal(String proposal) { this.proposal = proposal; }

    public BigDecimal getProposedBudget() { return proposedBudget; }
    public void setProposedBudget(BigDecimal proposedBudget) { this.proposedBudget = proposedBudget; }

    public Integer getEstimatedDays() { return estimatedDays; }
    public void setEstimatedDays(Integer estimatedDays) { this.estimatedDays = estimatedDays; }
}
