package com.freelancehub.controller;

import com.freelancehub.dto.ApiResponse;
import com.freelancehub.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<HashMap<String, Object>>> getPublicStats() {
        HashMap<String, Object> stats = dashboardService.getPublicStats();
        return ResponseEntity.ok(ApiResponse.success("Platform statistics retrieved", stats));
    }

    @GetMapping("/freelancer/{id}")
    public ResponseEntity<ApiResponse<HashMap<String, Object>>> getFreelancerDashboard(@PathVariable Long id) {
        HashMap<String, Object> data = dashboardService.getFreelancerDashboard(id);
        return ResponseEntity.ok(ApiResponse.success("Freelancer dashboard metrics retrieved", data));
    }

    @GetMapping("/client/{id}")
    public ResponseEntity<ApiResponse<HashMap<String, Object>>> getClientDashboard(@PathVariable Long id) {
        HashMap<String, Object> data = dashboardService.getClientDashboard(id);
        return ResponseEntity.ok(ApiResponse.success("Client dashboard metrics retrieved", data));
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<HashMap<String, Object>>> getAdminDashboard() {
        HashMap<String, Object> data = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard analytics retrieved", data));
    }
}
