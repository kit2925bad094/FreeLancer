package com.freelancehub.dto;

public class AuthResponse {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String profileImage;
    private Long profileId; // FreelancerProfile id or ClientProfile id

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String name, String email, String role, String profileImage, Long profileId) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.profileImage = profileImage;
        this.profileId = profileId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
}
