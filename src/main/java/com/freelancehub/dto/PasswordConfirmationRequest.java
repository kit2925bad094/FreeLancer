package com.freelancehub.dto;

import jakarta.validation.constraints.NotBlank;

public class PasswordConfirmationRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
}
