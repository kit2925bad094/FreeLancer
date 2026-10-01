package com.freelancehub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

public class ProjectCreateRequest {
    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Minimum budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum budget must be greater than 0")
    private BigDecimal budgetMin;

    @NotNull(message = "Maximum budget is required")
    private BigDecimal budgetMax;

    @NotNull(message = "Deadline is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date deadline;

    private ArrayList<Long> skillIds = new ArrayList<>();
    private ArrayList<String> skillNames = new ArrayList<>();

    public ProjectCreateRequest() {}

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

    public void setDeadline(String deadlineStr) {
        if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
            try {
                this.deadline = Date.valueOf(deadlineStr.trim().split("T")[0]);
            } catch (Exception e) {
                this.deadline = Date.valueOf(LocalDate.now().plusDays(30));
            }
        }
    }

    public ArrayList<Long> getSkillIds() { return skillIds; }
    public void setSkillIds(ArrayList<Long> skillIds) { this.skillIds = skillIds; }

    public ArrayList<String> getSkillNames() { return skillNames; }
    public void setSkillNames(ArrayList<String> skillNames) { this.skillNames = skillNames; }
}
