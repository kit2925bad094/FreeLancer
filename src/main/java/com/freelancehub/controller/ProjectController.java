package com.freelancehub.controller;

import com.freelancehub.dto.ApiResponse;
import com.freelancehub.dto.ProjectCreateRequest;
import com.freelancehub.dto.SkillMatchResponse;
import com.freelancehub.dto.StatusUpdateRequest;
import com.freelancehub.model.Project;
import com.freelancehub.model.ProjectStatusHistory;
import com.freelancehub.service.ProjectService;
import com.freelancehub.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectService projectService;
    private final SkillService skillService;

    public ProjectController(ProjectService projectService, SkillService skillService) {
        this.projectService = projectService;
        this.skillService = skillService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ArrayList<Project>>> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy) {

        ArrayList<Project> list;
        if (query != null || category != null || skill != null || minBudget != null || maxBudget != null || status != null || sortBy != null) {
            list = projectService.searchProjects(query, category, skill, minBudget, maxBudget, status, sortBy);
        } else {
            list = projectService.getAllProjects();
        }
        return ResponseEntity.ok(ApiResponse.success("Projects retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> getById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success("Project retrieved successfully", project));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Project>> create(@Valid @RequestBody ProjectCreateRequest request) {
        Project project = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully!", project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> update(@PathVariable Long id, @Valid @RequestBody ProjectCreateRequest request) {
        Project project = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Project>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        Project project = projectService.updateProjectStatus(id, request.getStatus(), request.getChangedBy());
        return ResponseEntity.ok(ApiResponse.success("Project status updated to " + project.getStatus(), project));
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<ApiResponse<ArrayList<ProjectStatusHistory>>> getStatusHistory(@PathVariable Long id) {
        ArrayList<ProjectStatusHistory> history = projectService.getStatusHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Project status history retrieved", history));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<ArrayList<Project>>> getByClient(@PathVariable Long clientId) {
        ArrayList<Project> list = projectService.getProjectsByClientId(clientId);
        return ResponseEntity.ok(ApiResponse.success("Client projects retrieved", list));
    }

    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<ApiResponse<ArrayList<Project>>> getByFreelancer(@PathVariable Long freelancerId) {
        ArrayList<Project> list = projectService.getProjectsByFreelancerId(freelancerId);
        return ResponseEntity.ok(ApiResponse.success("Assigned freelancer projects retrieved", list));
    }

    @GetMapping("/{projectId}/match/{freelancerId}")
    public ResponseEntity<ApiResponse<SkillMatchResponse>> getSkillMatch(
            @PathVariable Long projectId,
            @PathVariable Long freelancerId) {
        SkillMatchResponse match = skillService.calculateSkillMatch(projectId, freelancerId);
        return ResponseEntity.ok(ApiResponse.success("Skill match calculated", match));
    }
}
