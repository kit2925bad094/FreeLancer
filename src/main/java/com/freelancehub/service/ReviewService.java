package com.freelancehub.service;

import com.freelancehub.dto.ReviewCreateRequest;
import com.freelancehub.exception.BadRequestException;
import com.freelancehub.exception.ResourceNotFoundException;
import com.freelancehub.model.FreelancerProfile;
import com.freelancehub.model.Notification;
import com.freelancehub.model.Project;
import com.freelancehub.model.Review;
import com.freelancehub.model.User;
import com.freelancehub.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FreelancerRepository freelancerRepository;
    private final NotificationRepository notificationRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProjectRepository projectRepository,
                         UserRepository userRepository,
                         FreelancerRepository freelancerRepository,
                         NotificationRepository notificationRepository) {
        this.reviewRepository = reviewRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.freelancerRepository = freelancerRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Review createReview(ReviewCreateRequest req) {
        Project project = projectRepository.findById(req.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + req.getProjectId()));

        if (!"COMPLETED".equalsIgnoreCase(project.getStatus())) {
            throw new BadRequestException("Reviews can only be submitted after project completion. Current status: " + project.getStatus());
        }

        User reviewer = userRepository.findById(req.getReviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer user not found with id: " + req.getReviewerId()));

        User reviewee = userRepository.findById(req.getRevieweeId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewee user not found with id: " + req.getRevieweeId()));

        Review review = new Review();
        review.setProjectId(req.getProjectId());
        review.setReviewerId(req.getReviewerId());
        review.setRevieweeId(req.getRevieweeId());
        review.setRating(req.getRating());
        review.setComment(req.getComment().trim());

        Long reviewId = reviewRepository.save(review);
        review.setId(reviewId);
        review.setReviewerName(reviewer.getName());
        review.setReviewerRole(reviewer.getRole());
        review.setRevieweeName(reviewee.getName());
        review.setProjectTitle(project.getTitle());

        // Update rating for freelancer if reviewee is freelancer
        if ("FREELANCER".equalsIgnoreCase(reviewee.getRole())) {
            Optional<FreelancerProfile> fpOpt = freelancerRepository.findByUserId(reviewee.getId());
            if (fpOpt.isPresent()) {
                FreelancerProfile fp = fpOpt.get();
                double avgRating = reviewRepository.calculateAverageRatingForUser(reviewee.getId());
                int completed = fp.getCompletedProjects() != null ? fp.getCompletedProjects() + 1 : 1;
                freelancerRepository.updateRatingAndCompletedProjects(fp.getId(), avgRating, completed);
            }
        }

        // Send notification
        Notification notif = new Notification();
        notif.setUserId(reviewee.getId());
        notif.setTitle("New Review Received ⭐");
        notif.setMessage(reviewer.getName() + " left you a " + req.getRating() + "-star review for \"" + project.getTitle() + "\"");
        notif.setType("REVIEW");
        notificationRepository.save(notif);

        return review;
    }

    public ArrayList<Review> getReviewsByProject(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return reviewRepository.findByProjectId(projectId);
    }

    public ArrayList<Review> getReviewsByReviewee(Long revieweeId) {
        userRepository.findById(revieweeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + revieweeId));
        return reviewRepository.findByRevieweeId(revieweeId);
    }
}
