package com.freelancehub.controller;

import com.freelancehub.dto.ApiResponse;
import com.freelancehub.model.FreelancerProfile;
import com.freelancehub.service.FreelancerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/freelancers")
@CrossOrigin(origins = "*")
public class FreelancerController {

    private final FreelancerService freelancerService;

    public FreelancerController(FreelancerService freelancerService) {
        this.freelancerService = freelancerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ArrayList<FreelancerProfile>>> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String skill) {

        ArrayList<FreelancerProfile> list;
        if ((query != null && !query.trim().isEmpty()) || (skill != null && !skill.trim().isEmpty())) {
            list = freelancerService.searchFreelancers(query, skill);
        } else {
            list = freelancerService.getAllFreelancers();
        }
        return ResponseEntity.ok(ApiResponse.success("Freelancers retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FreelancerProfile>> getById(@PathVariable Long id) {
        FreelancerProfile profile = freelancerService.getFreelancerById(id);
        return ResponseEntity.ok(ApiResponse.success("Freelancer profile retrieved", profile));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<FreelancerProfile>> getByUserId(@PathVariable Long userId) {
        FreelancerProfile profile = freelancerService.getFreelancerByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Freelancer profile retrieved", profile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FreelancerProfile>> update(
            @PathVariable Long id,
            @RequestBody FreelancerProfile profile,
            @RequestParam(required = false) ArrayList<String> skills) {
        FreelancerProfile updated = freelancerService.updateFreelancer(id, profile, skills);
        return ResponseEntity.ok(ApiResponse.success("Freelancer profile updated successfully", updated));
    }
}
