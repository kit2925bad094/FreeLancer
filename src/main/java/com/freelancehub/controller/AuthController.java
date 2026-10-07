package com.freelancehub.controller;

import com.freelancehub.dto.ApiResponse;
import com.freelancehub.dto.AuthRequest;
import com.freelancehub.dto.AuthResponse;
import com.freelancehub.dto.PasswordConfirmationRequest;
import com.freelancehub.dto.ProfileUpdateRequest;
import com.freelancehub.dto.RegisterRequest;
import com.freelancehub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful! Welcome to FreelanceHub.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful!", response));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<AuthResponse>> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        AuthResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", response));
    }

    @DeleteMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordConfirmationRequest request) {
        userService.deleteProfile(userId, request.getCurrentPassword());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully."));
    }
}
