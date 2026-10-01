package com.freelancehub.service;

import com.freelancehub.dto.SkillMatchResponse;
import com.freelancehub.exception.BadRequestException;
import com.freelancehub.exception.ResourceNotFoundException;
import com.freelancehub.model.Skill;
import com.freelancehub.repository.ProjectRepository;
import com.freelancehub.repository.SkillRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;

    public SkillService(SkillRepository skillRepository, ProjectRepository projectRepository) {
        this.skillRepository = skillRepository;
        this.projectRepository = projectRepository;
    }

    public ArrayList<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    public Skill createSkill(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Skill name cannot be empty");
        }
        String clean = name.trim();
        Optional<Skill> existing = skillRepository.findByName(clean);
        if (existing.isPresent()) {
            return existing.get();
        }
        Long id = skillRepository.save(new Skill(null, clean));
        return new Skill(id, clean);
    }

    /**
     * Skill matching logic using HashSet operations as per Academic requirement:
     * - Project requires set of skills
     * - Freelancer has set of skills
     * - Intersection calculates matched skills
     * - Difference calculates missing skills
     * - Match % = (matched / totalRequired) * 100
     */
    public SkillMatchResponse calculateSkillMatch(Long projectId, Long freelancerId) {
        // Verify project exists
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        HashSet<String> projectSkills = skillRepository.findSkillNamesByProjectId(projectId);
        HashSet<String> freelancerSkills = skillRepository.findSkillNamesByFreelancerId(freelancerId);

        if (projectSkills.isEmpty()) {
            return new SkillMatchResponse(projectId, freelancerId, 100, 0, 0, new ArrayList<>(), new ArrayList<>());
        }

        // Demonstration of HashSet operations
        HashSet<String> matchedSet = new HashSet<>(projectSkills);
        matchedSet.retainAll(freelancerSkills); // Intersection

        HashSet<String> missingSet = new HashSet<>(projectSkills);
        missingSet.removeAll(freelancerSkills); // Difference

        int matchedCount = matchedSet.size();
        int totalRequired = projectSkills.size();
        int matchPercentage = (int) Math.round(((double) matchedCount / totalRequired) * 100.0);

        ArrayList<String> matchedList = new ArrayList<>(matchedSet);
        ArrayList<String> missingList = new ArrayList<>(missingSet);

        return new SkillMatchResponse(projectId, freelancerId, matchPercentage, matchedCount, totalRequired, matchedList, missingList);
    }
}
