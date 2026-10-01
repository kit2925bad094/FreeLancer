package com.freelancehub.service;

import com.freelancehub.dto.ProjectCreateRequest;
import com.freelancehub.exception.BadRequestException;
import com.freelancehub.exception.ResourceNotFoundException;
import com.freelancehub.model.*;
import com.freelancehub.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final ProjectStatusHistoryRepository statusHistoryRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final NotificationRepository notificationRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final FreelancerRepository freelancerRepository;

    public ProjectService(ProjectRepository projectRepository,
                          SkillRepository skillRepository,
                          ProjectStatusHistoryRepository statusHistoryRepository,
                          ProjectAssignmentRepository assignmentRepository,
                          NotificationRepository notificationRepository,
                          ClientProfileRepository clientProfileRepository,
                          FreelancerRepository freelancerRepository) {
        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.assignmentRepository = assignmentRepository;
        this.notificationRepository = notificationRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.freelancerRepository = freelancerRepository;
    }

    @Transactional
    public Project createProject(ProjectCreateRequest req) {
        // Validate client exists
        clientProfileRepository.findById(req.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found with id: " + req.getClientId()));

        if (req.getBudgetMax().compareTo(req.getBudgetMin()) < 0) {
            throw new BadRequestException("Maximum budget cannot be less than minimum budget");
        }

        Project p = new Project();
        p.setClientId(req.getClientId());
        p.setTitle(req.getTitle().trim());
        p.setDescription(req.getDescription().trim());
        p.setCategory(req.getCategory().trim());
        p.setBudgetMin(req.getBudgetMin());
        p.setBudgetMax(req.getBudgetMax());
        p.setDeadline(req.getDeadline());
        p.setStatus("OPEN");

        Long projectId = projectRepository.save(p);
        p.setId(projectId);

        // Record initial status in history
        ClientProfile client = clientProfileRepository.findById(req.getClientId()).orElse(null);
        Long userId = client != null ? client.getUserId() : null;
        statusHistoryRepository.save(projectId, "OPEN", userId);

        // Deduplicate skills using HashSet
        HashSet<Long> skillIdsToAdd = new HashSet<>();
        if (req.getSkillIds() != null) {
            skillIdsToAdd.addAll(req.getSkillIds());
        }

        if (req.getSkillNames() != null) {
            for (String sName : req.getSkillNames()) {
                if (sName != null && !sName.trim().isEmpty()) {
                    Optional<Skill> sk = skillRepository.findByName(sName.trim());
                    Long sId = sk.map(Skill::getId).orElseGet(() -> skillRepository.save(new Skill(null, sName.trim())));
                    skillIdsToAdd.add(sId);
                }
            }
        }

        for (Long sId : skillIdsToAdd) {
            skillRepository.addSkillToProject(projectId, sId);
        }

        p.setRequiredSkills(skillRepository.findSkillsByProjectId(projectId));
        return p;
    }

    public Project getProjectById(Long id) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        p.setRequiredSkills(skillRepository.findSkillsByProjectId(id));
        return p;
    }

    public ArrayList<Project> getAllProjects() {
        ArrayList<Project> list = projectRepository.findAll();
        for (Project p : list) {
            p.setRequiredSkills(skillRepository.findSkillsByProjectId(p.getId()));
        }
        return list;
    }

    public ArrayList<Project> searchProjects(String query, String category, String skill,
                                            BigDecimal minBudget, BigDecimal maxBudget, String status, String sortBy) {
        ArrayList<Project> list = projectRepository.searchAndFilter(query, category, skill, minBudget, maxBudget, status, sortBy);
        for (Project p : list) {
            p.setRequiredSkills(skillRepository.findSkillsByProjectId(p.getId()));
        }
        return list;
    }

    public ArrayList<Project> getProjectsByClientId(Long clientId) {
        ArrayList<Project> list = projectRepository.findByClientId(clientId);
        for (Project p : list) {
            p.setRequiredSkills(skillRepository.findSkillsByProjectId(p.getId()));
        }
        return list;
    }

    public ArrayList<Project> getProjectsByFreelancerId(Long freelancerId) {
        ArrayList<Project> list = projectRepository.findAssignedToFreelancer(freelancerId);
        for (Project p : list) {
            p.setRequiredSkills(skillRepository.findSkillsByProjectId(p.getId()));
        }
        return list;
    }

    @Transactional
    public Project updateProject(Long id, ProjectCreateRequest req) {
        Project existing = getProjectById(id);

        if (req.getTitle() != null) existing.setTitle(req.getTitle().trim());
        if (req.getDescription() != null) existing.setDescription(req.getDescription().trim());
        if (req.getCategory() != null) existing.setCategory(req.getCategory().trim());
        if (req.getBudgetMin() != null) existing.setBudgetMin(req.getBudgetMin());
        if (req.getBudgetMax() != null) existing.setBudgetMax(req.getBudgetMax());
        if (req.getDeadline() != null) existing.setDeadline(req.getDeadline());

        if (existing.getBudgetMax().compareTo(existing.getBudgetMin()) < 0) {
            throw new BadRequestException("Maximum budget cannot be less than minimum budget");
        }

        projectRepository.update(existing);

        if (req.getSkillNames() != null || req.getSkillIds() != null) {
            skillRepository.removeSkillsFromProject(id);
            HashSet<Long> skillIds = new HashSet<>();
            if (req.getSkillIds() != null) skillIds.addAll(req.getSkillIds());
            if (req.getSkillNames() != null) {
                for (String sName : req.getSkillNames()) {
                    if (sName != null && !sName.trim().isEmpty()) {
                        Optional<Skill> sk = skillRepository.findByName(sName.trim());
                        Long sId = sk.map(Skill::getId).orElseGet(() -> skillRepository.save(new Skill(null, sName.trim())));
                        skillIds.add(sId);
                    }
                }
            }
            for (Long sId : skillIds) {
                skillRepository.addSkillToProject(id, sId);
            }
        }

        existing.setRequiredSkills(skillRepository.findSkillsByProjectId(id));
        return existing;
    }

    public void deleteProject(Long id) {
        getProjectById(id);
        projectRepository.delete(id);
    }

    /**
     * Enforces valid status transitions as per requirement 23:
     * OPEN -> ASSIGNED, CANCELLED
     * ASSIGNED -> IN_PROGRESS, CANCELLED
     * IN_PROGRESS -> UNDER_REVIEW, CANCELLED
     * UNDER_REVIEW -> COMPLETED, IN_PROGRESS, CANCELLED
     * COMPLETED -> (Terminal state)
     */
    @Transactional
    public Project updateProjectStatus(Long projectId, String newStatus, Long changedByUserId) {
        Project project = getProjectById(projectId);
        String currentStatus = project.getStatus().toUpperCase();
        String targetStatus = newStatus.toUpperCase();

        if (currentStatus.equals(targetStatus)) {
            return project;
        }

        boolean isValid = false;
        switch (currentStatus) {
            case "OPEN":
                isValid = targetStatus.equals("ASSIGNED") || targetStatus.equals("CANCELLED");
                break;
            case "ASSIGNED":
                isValid = targetStatus.equals("IN_PROGRESS") || targetStatus.equals("CANCELLED");
                break;
            case "IN_PROGRESS":
                isValid = targetStatus.equals("UNDER_REVIEW") || targetStatus.equals("CANCELLED");
                break;
            case "UNDER_REVIEW":
                isValid = targetStatus.equals("COMPLETED") || targetStatus.equals("IN_PROGRESS") || targetStatus.equals("CANCELLED");
                break;
            case "CANCELLED":
            case "COMPLETED":
                isValid = false; // Terminal states
                break;
            default:
                isValid = false;
        }

        if (!isValid) {
            throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + targetStatus);
        }

        projectRepository.updateStatus(projectId, targetStatus);
        statusHistoryRepository.save(projectId, targetStatus, changedByUserId);

        // Trigger notifications to assigned freelancer or client
        Optional<ProjectAssignment> assignmentOpt = assignmentRepository.findByProjectId(projectId);
        if (assignmentOpt.isPresent()) {
            Long freelancerId = assignmentOpt.get().getFreelancerId();
            Optional<FreelancerProfile> fp = freelancerRepository.findById(freelancerId);
            if (fp.isPresent()) {
                Notification notif = new Notification();
                notif.setUserId(fp.get().getUserId());
                notif.setTitle("Project Status Updated");
                notif.setMessage("Project \"" + project.getTitle() + "\" is now " + targetStatus);
                notif.setType("PROJECT_UPDATE");
                notificationRepository.save(notif);
            }
        }

        project.setStatus(targetStatus);
        return project;
    }

    public ArrayList<ProjectStatusHistory> getStatusHistory(Long projectId) {
        getProjectById(projectId);
        return statusHistoryRepository.findByProjectId(projectId);
    }
}
