package com.freelancehub.service;

import com.freelancehub.dto.AuthRequest;
import com.freelancehub.dto.AuthResponse;
import com.freelancehub.dto.RegisterRequest;
import com.freelancehub.exception.BadRequestException;
import com.freelancehub.exception.ResourceNotFoundException;
import com.freelancehub.exception.UnauthorizedException;
import com.freelancehub.model.ClientProfile;
import com.freelancehub.model.FreelancerProfile;
import com.freelancehub.model.Skill;
import com.freelancehub.model.User;
import com.freelancehub.repository.ClientProfileRepository;
import com.freelancehub.repository.FreelancerRepository;
import com.freelancehub.repository.SkillRepository;
import com.freelancehub.repository.UserRepository;
import com.freelancehub.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FreelancerRepository freelancerRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final SkillRepository skillRepository;

    public UserService(UserRepository userRepository,
                       FreelancerRepository freelancerRepository,
                       ClientProfileRepository clientProfileRepository,
                       SkillRepository skillRepository) {
        this.userRepository = userRepository;
        this.freelancerRepository = freelancerRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        // Check if email already registered
        if (userRepository.findByEmail(req.getEmail().trim().toLowerCase()).isPresent()) {
            throw new BadRequestException("Email is already registered: " + req.getEmail());
        }

        // Create user entity
        User user = new User();
        user.setName(req.getName().trim());
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setPassword(PasswordUtil.hashPassword(req.getPassword()));
        user.setRole(req.getRole().toUpperCase());
        user.setProfileImage(req.getProfileImage() != null && !req.getProfileImage().trim().isEmpty()
            ? req.getProfileImage().trim()
            : null);
        user.setStatus("ACTIVE");

        Long userId = userRepository.save(user);
        Long profileId = null;

        if ("FREELANCER".equalsIgnoreCase(user.getRole())) {
            FreelancerProfile fp = new FreelancerProfile();
            fp.setUserId(userId);
            fp.setHeadline(req.getHeadline() != null ? req.getHeadline() : "Professional Freelancer");
            fp.setBio(req.getBio() != null ? req.getBio() : "");
            fp.setEducation(req.getEducation() != null ? req.getEducation() : "");
            fp.setExperience(req.getExperience() != null ? req.getExperience() : "1-3 Years");
            fp.setLocation(req.getLocation() != null ? req.getLocation() : "Remote");
            fp.setGithubUrl(req.getGithubUrl());
            fp.setLinkedinUrl(req.getLinkedinUrl());
            fp.setPortfolioUrl(req.getPortfolioUrl());
            fp.setHourlyRate(req.getHourlyRate());
            fp.setAvailability("AVAILABLE");

            profileId = freelancerRepository.save(fp);

            // Demonstrate HashSet for unique skill assignment
            if (req.getSkillNames() != null && !req.getSkillNames().isEmpty()) {
                HashSet<String> uniqueSkills = new HashSet<>();
                for (String sName : req.getSkillNames()) {
                    if (sName != null && !sName.trim().isEmpty()) {
                        uniqueSkills.add(sName.trim());
                    }
                }

                for (String sName : uniqueSkills) {
                    Optional<Skill> existing = skillRepository.findByName(sName);
                    Long skillId;
                    if (existing.isPresent()) {
                        skillId = existing.get().getId();
                    } else {
                        skillId = skillRepository.save(new Skill(null, sName));
                    }
                    skillRepository.addSkillToFreelancer(profileId, skillId);
                }
            }
        } else if ("CLIENT".equalsIgnoreCase(user.getRole())) {
            ClientProfile cp = new ClientProfile();
            cp.setUserId(userId);
            cp.setCompanyName(req.getCompanyName() != null ? req.getCompanyName() : req.getName());
            cp.setCompanyDescription(req.getCompanyDescription());
            cp.setWebsite(req.getWebsite());
            cp.setLocation(req.getLocation() != null ? req.getLocation() : "Remote");

            profileId = clientProfileRepository.save(cp);
        }

        String token = UUID.randomUUID().toString();
        return new AuthResponse(token, userId, user.getName(), user.getEmail(), user.getRole(), user.getProfileImage(), profileId);
    }

    public AuthResponse login(AuthRequest req) {
        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("No account found with this email. Please register first!"));

        if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
            throw new UnauthorizedException("Your account has been suspended by administration.");
        }

        if (!PasswordUtil.verify(req.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Incorrect password. Please check your credentials.");
        }

        Long profileId = null;
        if ("FREELANCER".equalsIgnoreCase(user.getRole())) {
            Optional<FreelancerProfile> fp = freelancerRepository.findByUserId(user.getId());
            if (fp.isPresent()) {
                profileId = fp.get().getId();
            }
        } else if ("CLIENT".equalsIgnoreCase(user.getRole())) {
            Optional<ClientProfile> cp = clientProfileRepository.findByUserId(user.getId());
            if (cp.isPresent()) {
                profileId = cp.get().getId();
            }
        }

        String token = UUID.randomUUID().toString();
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getProfileImage(), profileId);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public void updateUserStatus(Long id, String status) {
        if (!"ACTIVE".equalsIgnoreCase(status) && !"BLOCKED".equalsIgnoreCase(status)) {
            throw new BadRequestException("Status must be ACTIVE or BLOCKED");
        }
        User user = getUserById(id);
        userRepository.updateStatus(user.getId(), status.toUpperCase());
    }

    public ArrayList<User> getAllUsers() {
        return userRepository.findAll();
    }
}
