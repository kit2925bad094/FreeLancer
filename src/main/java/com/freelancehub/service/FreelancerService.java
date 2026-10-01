package com.freelancehub.service;

import com.freelancehub.exception.ResourceNotFoundException;
import com.freelancehub.model.FreelancerProfile;
import com.freelancehub.model.Skill;
import com.freelancehub.repository.FreelancerRepository;
import com.freelancehub.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

@Service
public class FreelancerService {

    private final FreelancerRepository freelancerRepository;
    private final SkillRepository skillRepository;

    public FreelancerService(FreelancerRepository freelancerRepository, SkillRepository skillRepository) {
        this.freelancerRepository = freelancerRepository;
        this.skillRepository = skillRepository;
    }

    public FreelancerProfile getFreelancerById(Long id) {
        FreelancerProfile profile = freelancerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found with id: " + id));
        profile.setSkills(skillRepository.findSkillsByFreelancerId(id));
        return profile;
    }

    public FreelancerProfile getFreelancerByUserId(Long userId) {
        FreelancerProfile profile = freelancerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found for user: " + userId));
        profile.setSkills(skillRepository.findSkillsByFreelancerId(profile.getId()));
        return profile;
    }

    public ArrayList<FreelancerProfile> getAllFreelancers() {
        ArrayList<FreelancerProfile> list = freelancerRepository.findAll();
        for (FreelancerProfile fp : list) {
            fp.setSkills(skillRepository.findSkillsByFreelancerId(fp.getId()));
        }
        return list;
    }

    public ArrayList<FreelancerProfile> searchFreelancers(String query, String skill) {
        ArrayList<FreelancerProfile> list = freelancerRepository.searchAndFilter(query, skill);
        for (FreelancerProfile fp : list) {
            fp.setSkills(skillRepository.findSkillsByFreelancerId(fp.getId()));
        }
        return list;
    }

    @Transactional
    public FreelancerProfile updateFreelancer(Long id, FreelancerProfile updated, ArrayList<String> skillNames) {
        FreelancerProfile existing = getFreelancerById(id);

        if (updated.getHeadline() != null) existing.setHeadline(updated.getHeadline());
        if (updated.getBio() != null) existing.setBio(updated.getBio());
        if (updated.getEducation() != null) existing.setEducation(updated.getEducation());
        if (updated.getExperience() != null) existing.setExperience(updated.getExperience());
        if (updated.getLocation() != null) existing.setLocation(updated.getLocation());
        if (updated.getGithubUrl() != null) existing.setGithubUrl(updated.getGithubUrl());
        if (updated.getLinkedinUrl() != null) existing.setLinkedinUrl(updated.getLinkedinUrl());
        if (updated.getPortfolioUrl() != null) existing.setPortfolioUrl(updated.getPortfolioUrl());
        if (updated.getHourlyRate() != null) existing.setHourlyRate(updated.getHourlyRate());
        if (updated.getAvailability() != null) existing.setAvailability(updated.getAvailability());

        freelancerRepository.update(existing);

        if (skillNames != null) {
            // Demonstrate HashSet for unique skill filtering
            HashSet<String> uniqueSkillNames = new HashSet<>();
            for (String sName : skillNames) {
                if (sName != null && !sName.trim().isEmpty()) {
                    uniqueSkillNames.add(sName.trim());
                }
            }

            skillRepository.removeSkillsFromFreelancer(id);
            for (String sName : uniqueSkillNames) {
                Optional<Skill> skillOpt = skillRepository.findByName(sName);
                Long skillId;
                if (skillOpt.isPresent()) {
                    skillId = skillOpt.get().getId();
                } else {
                    skillId = skillRepository.save(new Skill(null, sName));
                }
                skillRepository.addSkillToFreelancer(id, skillId);
            }
        }

        existing.setSkills(skillRepository.findSkillsByFreelancerId(id));
        return existing;
    }
}
