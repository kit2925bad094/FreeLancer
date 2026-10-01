package com.freelancehub.controller;

import com.freelancehub.dto.ApiResponse;
import com.freelancehub.dto.ReviewCreateRequest;
import com.freelancehub.model.Review;
import com.freelancehub.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> create(@Valid @RequestBody ReviewCreateRequest request) {
        Review review = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", review));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<ArrayList<Review>>> getByProject(@PathVariable Long projectId) {
        ArrayList<Review> reviews = reviewService.getReviewsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project reviews retrieved", reviews));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<ArrayList<Review>>> getByUser(@PathVariable Long userId) {
        ArrayList<Review> reviews = reviewService.getReviewsByReviewee(userId);
        return ResponseEntity.ok(ApiResponse.success("User reviews retrieved", reviews));
    }
}
