package com.freelancehub.controller;

import com.freelancehub.dto.ApiResponse;
import com.freelancehub.model.ClientProfile;
import com.freelancehub.repository.ClientProfileRepository;
import com.freelancehub.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
public class ClientProfileController {

    private final ClientProfileRepository clientProfileRepository;

    public ClientProfileController(ClientProfileRepository clientProfileRepository) {
        this.clientProfileRepository = clientProfileRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<ClientProfile>> getByUserId(@PathVariable Long userId) {
        ClientProfile profile = clientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client profile not found for user: " + userId));
        return ResponseEntity.ok(ApiResponse.success("Client profile retrieved", profile));
    }
}
