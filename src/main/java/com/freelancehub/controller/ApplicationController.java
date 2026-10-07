package com.freelancehub.controller;

import com.freelancehub.dto.ApiResponse;
import com.freelancehub.dto.ApplicationCreateRequest;
import com.freelancehub.model.ProjectApplication;
import com.freelancehub.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/api/projects/{projectId}/apply")
    public ResponseEntity<ApiResponse<ProjectApplication>> apply(
            @PathVariable Long projectId,
            @Valid @RequestBody ApplicationCreateRequest request) {
        ProjectApplication app = applicationService.apply(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully!", app));
    }

    @GetMapping("/api/projects/{projectId}/applications")
    public ResponseEntity<ApiResponse<ArrayList<ProjectApplication>>> getByProject(@PathVariable Long projectId) {
        ArrayList<ProjectApplication> apps = applicationService.getApplicationsByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project applications retrieved", apps));
    }

    @GetMapping("/api/freelancers/{freelancerId}/applications")
    public ResponseEntity<ApiResponse<ArrayList<ProjectApplication>>> getByFreelancer(@PathVariable Long freelancerId) {
        ArrayList<ProjectApplication> apps = applicationService.getApplicationsByFreelancerId(freelancerId);
        return ResponseEntity.ok(ApiResponse.success("Freelancer applications retrieved", apps));
    }

    @PutMapping("/api/applications/{id}/accept")
    public ResponseEntity<ApiResponse<ProjectApplication>> accept(
            @PathVariable Long id,
            @RequestParam(required = false) Long clientId) {
        ProjectApplication app = applicationService.acceptApplication(id, clientId);
        return ResponseEntity.ok(ApiResponse.success("Application accepted! Project assigned to freelancer.", app));
    }

    @PutMapping("/api/applications/{id}/reject")
    public ResponseEntity<ApiResponse<ProjectApplication>> reject(
            @PathVariable Long id,
            @RequestParam(required = false) Long clientId) {
        ProjectApplication app = applicationService.rejectApplication(id, clientId);
        return ResponseEntity.ok(ApiResponse.success("Application rejected.", app));
    }
}
