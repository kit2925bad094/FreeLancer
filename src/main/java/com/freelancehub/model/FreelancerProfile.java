package com.freelancehub.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;

public class FreelancerProfile {
    private Long id;
    private Long userId;
    private String headline;
    private String bio;
    private String education;
    private String experience;
    private String location;
    private String githubUrl;
    private String linkedinUrl;
    private String portfolioUrl;
    private BigDecimal hourlyRate;
    private String availability;
    private BigDecimal rating;
    private Integer completedProjects;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined user details
    private String name;
    private String email;
    private String profileImage;
    private String status;
    private ArrayList<Skill> skills = new ArrayList<>();

    public FreelancerProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getCompletedProjects() { return completedProjects; }
    public void setCompletedProjects(Integer completedProjects) { this.completedProjects = completedProjects; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public ArrayList<Skill> getSkills() { return skills; }
    public void setSkills(ArrayList<Skill> skills) { this.skills = skills; }
}
