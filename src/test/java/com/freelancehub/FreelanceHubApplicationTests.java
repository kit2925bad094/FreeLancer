package com.freelancehub;

import com.freelancehub.dto.ApplicationCreateRequest;
import com.freelancehub.dto.AuthRequest;
import com.freelancehub.dto.AuthResponse;
import com.freelancehub.dto.ProjectCreateRequest;
import com.freelancehub.dto.RegisterRequest;
import com.freelancehub.dto.SkillMatchResponse;
import com.freelancehub.exception.BadRequestException;
import com.freelancehub.exception.UnauthorizedException;
import com.freelancehub.model.Project;
import com.freelancehub.model.ProjectApplication;
import com.freelancehub.service.ApplicationService;
import com.freelancehub.service.ProjectService;
import com.freelancehub.service.SkillService;
import com.freelancehub.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class FreelanceHubApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private SkillService skillService;

    private AuthResponse createClient(String prefix) {
        RegisterRequest req = new RegisterRequest();
        req.setName(prefix + " Client");
        req.setEmail(prefix.toLowerCase() + "_" + System.nanoTime() + "@client.com");
        req.setPassword("password123");
        req.setRole("CLIENT");
        req.setCompanyName(prefix + " Technologies");
        return userService.register(req);
    }

    private AuthResponse createFreelancer(String prefix, List<String> skills) {
        RegisterRequest req = new RegisterRequest();
        req.setName(prefix + " Dev");
        req.setEmail(prefix.toLowerCase() + "_" + System.nanoTime() + "@dev.com");
        req.setPassword("password123");
        req.setRole("FREELANCER");
        req.setHeadline("Full Stack Engineer");
        req.setHourlyRate(new BigDecimal("1200.00"));
        req.setSkillNames(new ArrayList<>(skills));
        return userService.register(req);
    }

    private Project createProject(Long clientId, List<String> skills) {
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setClientId(clientId);
        req.setTitle("Project " + System.nanoTime());
        req.setDescription("Building an interactive dashboard with Spring Boot and JDBC");
        req.setCategory("Web Development");
        req.setBudgetMin(new BigDecimal("30000.00"));
        req.setBudgetMax(new BigDecimal("50000.00"));
        req.setDeadline(Date.valueOf(LocalDate.now().plusDays(20)));
        req.setSkillNames(new ArrayList<>(skills));
        return projectService.createProject(req);
    }

    @Test
    @DisplayName("1. Freelancer Registration Test")
    void testFreelancerRegistration() {
        AuthResponse res = createFreelancer("Alpha", List.of("Java", "Spring Boot"));
        assertNotNull(res);
        assertNotNull(res.getUserId());
        assertNotNull(res.getProfileId());
        assertEquals("Alpha Dev", res.getName());
        assertEquals("FREELANCER", res.getRole());
    }

    @Test
    @DisplayName("2. Project Creation Test")
    void testProjectCreation() {
        AuthResponse client = createClient("Beta");
        Project project = createProject(client.getProfileId(), List.of("Java", "React"));

        assertNotNull(project);
        assertNotNull(project.getId());
        assertEquals("OPEN", project.getStatus());
        assertEquals(client.getProfileId(), project.getClientId());
    }

    @Test
    @DisplayName("3. Skill Matching Test (Demonstrating HashSet)")
    void testSkillMatching() {
        AuthResponse client = createClient("Gamma");
        Project project = createProject(client.getProfileId(), List.of("Java", "Spring Boot", "SQL"));

        AuthResponse freelancer = createFreelancer("Delta", List.of("Java", "Spring Boot", "SQL", "React"));

        SkillMatchResponse match = skillService.calculateSkillMatch(project.getId(), freelancer.getProfileId());
        assertNotNull(match);
        assertEquals(100.0, match.getMatchPercentage(), 0.01, "Expected 100% match since freelancer has all required skills");
        assertTrue(match.getMatchedSkills().contains("java"));
        assertTrue(match.getMatchedSkills().contains("spring boot"));
        assertTrue(match.getMatchedSkills().contains("sql"));
    }

    @Test
    @DisplayName("4. Duplicate Application Prevention Test")
    void testDuplicateApplicationPrevention() {
        AuthResponse client = createClient("Epsilon");
        Project project = createProject(client.getProfileId(), List.of("Java"));
        AuthResponse freelancer = createFreelancer("Zeta", List.of("Java"));

        // First application
        ApplicationCreateRequest appReq = new ApplicationCreateRequest(
                freelancer.getProfileId(), "My proposal", new BigDecimal("25000.00"), 10);
        ProjectApplication app1 = applicationService.apply(project.getId(), appReq);
        assertNotNull(app1);

        // Second application by the SAME freelancer should throw BadRequestException
        assertThrows(BadRequestException.class, () -> {
            applicationService.apply(project.getId(), appReq);
        });
    }

    @Test
    @DisplayName("5. Application Acceptance & Project Assignment Test")
    void testApplicationAcceptance() {
        AuthResponse client = createClient("Eta");
        Project project = createProject(client.getProfileId(), List.of("Python"));
        AuthResponse freelancer = createFreelancer("Theta", List.of("Python"));

        ApplicationCreateRequest appReq = new ApplicationCreateRequest(
                freelancer.getProfileId(), "I will build this", new BigDecimal("45000.00"), 12);
        ProjectApplication app = applicationService.apply(project.getId(), appReq);
        assertNotNull(app);

        // Accept application by client owner
        ProjectApplication accepted = applicationService.acceptApplication(app.getId(), client.getProfileId());
        assertEquals("ACCEPTED", accepted.getStatus());

        // Verify project is now ASSIGNED
        Project updatedProject = projectService.getProjectById(project.getId());
        assertEquals("ASSIGNED", updatedProject.getStatus());
        assertEquals(freelancer.getProfileId(), updatedProject.getAssignedFreelancerId());
    }

    @Test
    @DisplayName("6. Status Update Transition Rules Test")
    void testStatusUpdate() {
        AuthResponse client = createClient("Iota");
        Project project = createProject(client.getProfileId(), List.of("Java"));
        AuthResponse freelancer = createFreelancer("Kappa", List.of("Java"));

        ApplicationCreateRequest appReq = new ApplicationCreateRequest(
                freelancer.getProfileId(), "Proposal", new BigDecimal("30000.00"), 10);
        ProjectApplication app = applicationService.apply(project.getId(), appReq);
        applicationService.acceptApplication(app.getId(), client.getProfileId());

        // Status is now ASSIGNED
        // Valid transition: ASSIGNED -> IN_PROGRESS
        Project updated = projectService.updateProjectStatus(project.getId(), "IN_PROGRESS", client.getUserId());
        assertEquals("IN_PROGRESS", updated.getStatus());

        // Invalid transition: IN_PROGRESS -> COMPLETED directly (must go to UNDER_REVIEW first)
        assertThrows(BadRequestException.class, () -> {
            projectService.updateProjectStatus(project.getId(), "COMPLETED", client.getUserId());
        });
    }

    @Test
    @DisplayName("7. Unauthorized Access Test")
    void testUnauthorizedAccess() {
        // Unregistered email
        AuthRequest invalidEmailReq = new AuthRequest("nonexistent_" + System.nanoTime() + "@test.com", "any_password");
        assertThrows(UnauthorizedException.class, () -> {
            userService.login(invalidEmailReq);
        });

        // Registered user but wrong password
        AuthResponse user = createFreelancer("Lambda", List.of("Java"));
        AuthRequest wrongPasswordReq = new AuthRequest(user.getEmail(), "incorrect_password_xyz");
        assertThrows(UnauthorizedException.class, () -> {
            userService.login(wrongPasswordReq);
        });
    }
}
