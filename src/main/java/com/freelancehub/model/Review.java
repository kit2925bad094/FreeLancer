package com.freelancehub.model;

import java.sql.Timestamp;

public class Review {
    private Long id;
    private Long projectId;
    private Long reviewerId;
    private Long revieweeId;
    private Integer rating; // 1 to 5
    private String comment;
    private Timestamp createdAt;

    private String reviewerName;
    private String reviewerRole;
    private String revieweeName;
    private String projectTitle;

    public Review() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public Long getRevieweeId() { return revieweeId; }
    public void setRevieweeId(Long revieweeId) { this.revieweeId = revieweeId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getReviewerRole() { return reviewerRole; }
    public void setReviewerRole(String reviewerRole) { this.reviewerRole = reviewerRole; }

    public String getRevieweeName() { return revieweeName; }
    public void setRevieweeName(String revieweeName) { this.revieweeName = revieweeName; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
}
