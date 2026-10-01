package com.freelancehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MessageCreateRequest {
    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Message text is required")
    private String message;

    public MessageCreateRequest() {}

    public MessageCreateRequest(Long senderId, Long receiverId, Long projectId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.projectId = projectId;
        this.message = message;
    }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
