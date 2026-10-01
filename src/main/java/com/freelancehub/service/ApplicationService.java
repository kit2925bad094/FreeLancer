package com.freelancehub.service;

import com.freelancehub.dto.ApplicationCreateRequest;
import com.freelancehub.dto.SkillMatchResponse;
import com.freelancehub.exception.BadRequestException;
import com.freelancehub.exception.ResourceNotFoundException;
import com.freelancehub.model.*;
import com.freelancehub.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final FreelancerRepository freelancerRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final ProjectStatusHistoryRepository statusHistoryRepository;
    private final NotificationRepository notificationRepository;
    private final SkillRepository skillRepository;
    private final SkillService skillService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ProjectRepository projectRepository,
                              FreelancerRepository freelancerRepository,
                              ClientProfileRepository clientProfileRepository,
                              ProjectAssignmentRepository assignmentRepository,
                              ProjectStatusHistoryRepository statusHistoryRepository,
                              NotificationRepository notificationRepository,
                              SkillRepository skillRepository,
                              SkillService skillService) {
        this.applicationRepository = applicationRepository;
        this.projectRepository = projectRepository;
        this.freelancerRepository = freelancerRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.assignmentRepository = assignmentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.notificationRepository = notificationRepository;
        this.skillRepository = skillRepository;
        this.skillService = skillService;
    }

    @Transactional
    public ProjectApplication apply(Long projectId, ApplicationCreateRequest req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (!"OPEN".equalsIgnoreCase(project.getStatus())) {
            throw new BadRequestException("Applications are only accepted for OPEN projects. Current status: " + project.getStatus());
        }

        FreelancerProfile freelancer = freelancerRepository.findById(req.getFreelancerId())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found with id: " + req.getFreelancerId()));

        // Demonstration of HashSet requirement for duplicate application check in backend logic
        HashSet<Long> existingApplicantIds = applicationRepository.findApplicantFreelancerIdsByProjectId(projectId);
        if (existingApplicantIds.contains(req.getFreelancerId())) {
            throw new BadRequestException("You have already applied for this project.");
        }

        ProjectApplication app = new ProjectApplication();
        app.setProjectId(projectId);
        app.setFreelancerId(req.getFreelancerId());
        app.setProposal(req.getProposal().trim());
        app.setProposedBudget(req.getProposedBudget());
        app.setEstimatedDays(req.getEstimatedDays());
        app.setStatus("PENDING");

        Long appId = applicationRepository.save(app);
        app.setId(appId);

        // Notify client
        Optional<ClientProfile> clientOpt = clientProfileRepository.findById(project.getClientId());
        if (clientOpt.isPresent()) {
            Notification notif = new Notification();
            notif.setUserId(clientOpt.get().getUserId());
            notif.setTitle("New Applicant Received 🚀");
            notif.setMessage(freelancer.getName() + " applied for \"" + project.getTitle() + "\" with a proposal of ₹" + req.getProposedBudget());
            notif.setType("APPLICATION");
            notificationRepository.save(notif);
        }

        return app;
    }

    public ArrayList<ProjectApplication> getApplicationsByProjectId(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        ArrayList<ProjectApplication> apps = applicationRepository.findByProjectId(projectId);
        for (ProjectApplication app : apps) {
            app.setFreelancerSkills(skillRepository.findSkillsByFreelancerId(app.getFreelancerId()));
            SkillMatchResponse match = skillService.calculateSkillMatch(projectId, app.getFreelancerId());
            app.setSkillMatchPercentage(match.getMatchPercentage());
        }
        return apps;
    }

    public ArrayList<ProjectApplication> getApplicationsByFreelancerId(Long freelancerId) {
        freelancerRepository.findById(freelancerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found with id: " + freelancerId));

        ArrayList<ProjectApplication> apps = applicationRepository.findByFreelancerId(freelancerId);
        for (ProjectApplication app : apps) {
            SkillMatchResponse match = skillService.calculateSkillMatch(app.getProjectId(), freelancerId);
            app.setSkillMatchPercentage(match.getMatchPercentage());
        }
        return apps;
    }

    @Transactional
    public ProjectApplication acceptApplication(Long applicationId, Long clientId) {
        ProjectApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        Project project = projectRepository.findById(app.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + app.getProjectId()));

        // Check client authorization if provided
        if (clientId != null && !project.getClientId().equals(clientId)) {
            throw new BadRequestException("Unauthorized: Only the project owner can accept applications.");
        }

        // 1. Update application status
        applicationRepository.updateStatus(applicationId, "ACCEPTED");
        app.setStatus("ACCEPTED");

        // 2. Create project assignment
        assignmentRepository.save(project.getId(), app.getFreelancerId());

        // 3. Change project status to ASSIGNED
        projectRepository.updateStatus(project.getId(), "ASSIGNED");

        // 4. Add status history record
        ClientProfile client = clientProfileRepository.findById(project.getClientId()).orElse(null);
        Long userId = client != null ? client.getUserId() : null;
        statusHistoryRepository.save(project.getId(), "ASSIGNED", userId);

        // 5. Create notification for freelancer
        Optional<FreelancerProfile> fp = freelancerRepository.findById(app.getFreelancerId());
        if (fp.isPresent()) {
            Notification notif = new Notification();
            notif.setUserId(fp.get().getUserId());
            notif.setTitle("Application Accepted! 🎉");
            notif.setMessage("Congratulations! Your proposal for \"" + project.getTitle() + "\" was accepted. The project is now assigned to you.");
            notif.setType("APPLICATION");
            notificationRepository.save(notif);
        }

        return app;
    }

    @Transactional
    public ProjectApplication rejectApplication(Long applicationId, Long clientId) {
        ProjectApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        Project project = projectRepository.findById(app.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + app.getProjectId()));

        if (clientId != null && !project.getClientId().equals(clientId)) {
            throw new BadRequestException("Unauthorized: Only the project owner can reject applications.");
        }

        applicationRepository.updateStatus(applicationId, "REJECTED");
        app.setStatus("REJECTED");

        // Notify freelancer
        Optional<FreelancerProfile> fp = freelancerRepository.findById(app.getFreelancerId());
        if (fp.isPresent()) {
            Notification notif = new Notification();
            notif.setUserId(fp.get().getUserId());
            notif.setTitle("Application Status Update");
            notif.setMessage("Your application for \"" + project.getTitle() + "\" was not selected at this time.");
            notif.setType("APPLICATION");
            notificationRepository.save(notif);
        }

        return app;
    }
}
