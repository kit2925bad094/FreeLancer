package com.freelancehub.service;

import com.freelancehub.model.*;
import com.freelancehub.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final FreelancerRepository freelancerRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;

    public DashboardService(UserRepository userRepository,
                            FreelancerRepository freelancerRepository,
                            ClientProfileRepository clientProfileRepository,
                            ProjectRepository projectRepository,
                            ApplicationRepository applicationRepository,
                            PaymentRepository paymentRepository,
                            NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.freelancerRepository = freelancerRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.projectRepository = projectRepository;
        this.applicationRepository = applicationRepository;
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Public stats for landing page (loaded dynamically from database).
     */
    public HashMap<String, Object> getPublicStats() {
        HashMap<String, Object> stats = new HashMap<>();

        int totalFreelancers = freelancerRepository.countAll();
        int totalClients = clientProfileRepository.countAll();
        int totalProjects = projectRepository.countTotal();
        int completedProjects = projectRepository.countByStatus("COMPLETED");

        stats.put("totalFreelancers", totalFreelancers);
        stats.put("totalClients", totalClients);
        stats.put("totalProjects", totalProjects);
        stats.put("completedProjects", completedProjects);

        return stats;
    }

    /**
     * Freelancer Dashboard metrics and active projects with progress percentage.
     */
    public HashMap<String, Object> getFreelancerDashboard(Long freelancerId) {
        HashMap<String, Object> dashboard = new HashMap<>();

        // 1. Core KPIs
        int appliedCount = applicationRepository.countByFreelancerId(freelancerId);
        ArrayList<Project> assignedProjects = projectRepository.findAssignedToFreelancer(freelancerId);

        int activeCount = 0;
        int completedCount = 0;
        ArrayList<HashMap<String, Object>> activeProjectsList = new ArrayList<>();

        for (Project p : assignedProjects) {
            String status = p.getStatus().toUpperCase();
            if ("COMPLETED".equals(status)) {
                completedCount++;
            } else if (!"CANCELLED".equals(status)) {
                activeCount++;

                // Map status to visual completion percentage
                HashMap<String, Object> projCard = new HashMap<>();
                projCard.put("id", p.getId());
                projCard.put("title", p.getTitle());
                projCard.put("category", p.getCategory());
                projCard.put("status", p.getStatus());
                projCard.put("deadline", p.getDeadline());
                projCard.put("clientCompanyName", p.getClientCompanyName());

                int progressPct = 25; // default ASSIGNED
                if ("IN_PROGRESS".equals(status)) progressPct = 65;
                else if ("UNDER_REVIEW".equals(status)) progressPct = 90;
                else if ("COMPLETED".equals(status)) progressPct = 100;

                projCard.put("progressPercent", progressPct);
                activeProjectsList.add(projCard);
            }
        }

        BigDecimal totalEarnings = paymentRepository.sumEarningsByFreelancer(freelancerId);

        dashboard.put("appliedProjects", appliedCount);
        dashboard.put("activeProjects", activeCount);
        dashboard.put("completedProjects", completedCount);
        dashboard.put("totalEarnings", totalEarnings);
        dashboard.put("myActiveProjects", activeProjectsList);

        // Recent applications
        ArrayList<ProjectApplication> applications = applicationRepository.findByFreelancerId(freelancerId);
        dashboard.put("recentApplications", applications);

        return dashboard;
    }

    /**
     * Client Dashboard stats, project status counts, spending, and recent applicants.
     */
    public HashMap<String, Object> getClientDashboard(Long clientId) {
        HashMap<String, Object> dashboard = new HashMap<>();

        ArrayList<Project> clientProjects = projectRepository.findByClientId(clientId);

        // Required demonstration of HashMap for status distribution
        HashMap<String, Integer> statusDistribution = new HashMap<>();
        statusDistribution.put("OPEN", 0);
        statusDistribution.put("ASSIGNED", 0);
        statusDistribution.put("IN_PROGRESS", 0);
        statusDistribution.put("UNDER_REVIEW", 0);
        statusDistribution.put("COMPLETED", 0);
        statusDistribution.put("CANCELLED", 0);

        for (Project p : clientProjects) {
            String status = p.getStatus().toUpperCase();
            statusDistribution.put(status, statusDistribution.getOrDefault(status, 0) + 1);
        }

        int totalProjects = clientProjects.size();
        int openCount = statusDistribution.get("OPEN");
        int inProgressCount = statusDistribution.get("ASSIGNED") + statusDistribution.get("IN_PROGRESS") + statusDistribution.get("UNDER_REVIEW");
        int completedCount = statusDistribution.get("COMPLETED");
        BigDecimal totalSpent = paymentRepository.sumSpendingByClient(clientId);

        dashboard.put("totalProjects", totalProjects);
        dashboard.put("openProjects", openCount);
        dashboard.put("inProgressProjects", inProgressCount);
        dashboard.put("completedProjects", completedCount);
        dashboard.put("totalSpent", totalSpent);
        dashboard.put("statusDistribution", statusDistribution);
        dashboard.put("projects", clientProjects);

        // Recent applicants across all client projects
        ArrayList<ProjectApplication> recentApplicants = new ArrayList<>();
        for (Project p : clientProjects) {
            ArrayList<ProjectApplication> apps = applicationRepository.findByProjectId(p.getId());
            recentApplicants.addAll(apps);
        }
        recentApplicants.sort((a, b) -> b.getAppliedAt().compareTo(a.getAppliedAt()));
        dashboard.put("recentApplicants", recentApplicants.size() > 10 ? new ArrayList<>(recentApplicants.subList(0, 10)) : recentApplicants);

        return dashboard;
    }

    /**
     * Admin Dashboard analytics: users, roles, categories, projects, chart data.
     */
    public HashMap<String, Object> getAdminDashboard() {
        HashMap<String, Object> adminData = new HashMap<>();

        int totalUsers = userRepository.countAll();
        int totalFreelancers = freelancerRepository.countAll();
        int totalClients = clientProfileRepository.countAll();
        int totalProjects = projectRepository.countTotal();
        int completedProjects = projectRepository.countByStatus("COMPLETED");
        int activeProjects = totalProjects - completedProjects - projectRepository.countByStatus("CANCELLED");
        BigDecimal totalPlatformVolume = paymentRepository.sumTotalPaidPlatform();

        adminData.put("totalUsers", totalUsers);
        adminData.put("totalFreelancers", totalFreelancers);
        adminData.put("totalClients", totalClients);
        adminData.put("totalProjects", totalProjects);
        adminData.put("completedProjects", completedProjects);
        adminData.put("activeProjects", Math.max(0, activeProjects));
        adminData.put("totalPlatformVolume", totalPlatformVolume);

        // Status mapping using HashMap
        HashMap<String, Integer> projectsByStatus = new HashMap<>();
        String[] statuses = {"OPEN", "ASSIGNED", "IN_PROGRESS", "UNDER_REVIEW", "COMPLETED", "CANCELLED"};
        for (String s : statuses) {
            projectsByStatus.put(s, projectRepository.countByStatus(s));
        }
        adminData.put("projectsByStatus", projectsByStatus);

        // Role mapping using HashMap
        HashMap<String, Integer> usersByRole = new HashMap<>();
        usersByRole.put("FREELANCER", userRepository.countByRole("FREELANCER"));
        usersByRole.put("CLIENT", userRepository.countByRole("CLIENT"));
        usersByRole.put("ADMIN", userRepository.countByRole("ADMIN"));
        adminData.put("usersByRole", usersByRole);

        // Category breakdown using HashMap
        ArrayList<Project> allProjects = projectRepository.findAll();
        HashMap<String, Integer> projectsByCategory = new HashMap<>();
        for (Project p : allProjects) {
            String cat = p.getCategory() != null ? p.getCategory() : "General";
            projectsByCategory.put(cat, projectsByCategory.getOrDefault(cat, 0) + 1);
        }
        adminData.put("projectsByCategory", projectsByCategory);

        return adminData;
    }
}
