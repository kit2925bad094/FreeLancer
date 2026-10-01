package com.freelancehub.controller;

import com.freelancehub.dto.ApiResponse;
import com.freelancehub.model.User;
import com.freelancehub.service.ProjectService;
import com.freelancehub.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;
    private final ProjectService projectService;

    public AdminController(UserService userService, ProjectService projectService) {
        this.userService = userService;
        this.projectService = projectService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<ArrayList<User>>> getAllUsers() {
        ArrayList<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        userService.updateUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("User status updated to " + status));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted by admin"));
    }
}
