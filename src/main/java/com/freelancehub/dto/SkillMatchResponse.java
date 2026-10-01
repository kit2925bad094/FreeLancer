package com.freelancehub.dto;

import java.util.ArrayList;

public class SkillMatchResponse {
    private Long projectId;
    private Long freelancerId;
    private int matchPercentage;
    private int matchedCount;
    private int totalRequired;
    private ArrayList<String> matchedSkills = new ArrayList<>();
    private ArrayList<String> missingSkills = new ArrayList<>();

    public SkillMatchResponse() {}

    public SkillMatchResponse(Long projectId, Long freelancerId, int matchPercentage, int matchedCount, int totalRequired, ArrayList<String> matchedSkills, ArrayList<String> missingSkills) {
        this.projectId = projectId;
        this.freelancerId = freelancerId;
        this.matchPercentage = matchPercentage;
        this.matchedCount = matchedCount;
        this.totalRequired = totalRequired;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public int getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(int matchPercentage) { this.matchPercentage = matchPercentage; }

    public int getMatchedCount() { return matchedCount; }
    public void setMatchedCount(int matchedCount) { this.matchedCount = matchedCount; }

    public int getTotalRequired() { return totalRequired; }
    public void setTotalRequired(int totalRequired) { this.totalRequired = totalRequired; }

    public ArrayList<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(ArrayList<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public ArrayList<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(ArrayList<String> missingSkills) { this.missingSkills = missingSkills; }
}
