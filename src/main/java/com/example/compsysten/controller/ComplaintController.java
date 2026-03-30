package com.example.compsysten.controller;

import com.example.compsysten.dto.ComplaintDTO;
import com.example.compsysten.dto.ComplaintResponseDTO;
import com.example.compsysten.dto.StatusUpdateRequest;
import com.example.compsysten.model.Complaint;
import com.example.compsysten.model.ComplaintStatus;
import com.example.compsysten.model.User;
import com.example.compsysten.security.CustomUserDetails;
import com.example.compsysten.service.ComplaintService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private static final Logger logger = LoggerFactory.getLogger(ComplaintController.class);

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // -------------------------------------------------------------------------
    // POST /api/complaints  – STUDENT creates a complaint
    // -------------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> createComplaint(@Valid @RequestBody ComplaintDTO complaintDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            logger.info("POST /api/complaints - Authentication: {}", authentication);
            logger.info("Is Authenticated: {}", authentication != null && authentication.isAuthenticated());

            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Authentication failed - returning 401");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated. Please log in first."));
            }

            Object principal = authentication.getPrincipal();
            logger.info("Principal type: {}, Principal: {}", principal.getClass().getSimpleName(), principal);

            if (!(principal instanceof CustomUserDetails)) {
                logger.warn("Invalid principal type - expecting CustomUserDetails");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid authentication principal"));
            }

            CustomUserDetails userDetails = (CustomUserDetails) principal;
            User user = userDetails.getUser();

            if (user == null || user.getId() == null || user.getId() <= 0) {
                logger.error("User object is invalid or has no ID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User authentication is invalid"));
            }

            logger.info("User authenticated: {} (ID: {}), Role: {}", user.getEmail(), user.getId(), user.getRole());
            logger.info("User authorities: {}", userDetails.getAuthorities());

            // Only students can file complaints
            if (!user.getRole().toString().equals("STUDENT")) {
                logger.warn("User {} tried to file complaint with role {}", user.getEmail(), user.getRole());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only students can file complaints. Your role is: " + user.getRole()));
            }

            Complaint complaint = new Complaint();
            complaint.setTitle(complaintDTO.getTitle());
            complaint.setDescription(complaintDTO.getDescription());
            complaint.setCategory(complaintDTO.getCategory());
            complaint.setUser(user);

            Complaint savedComplaint = complaintService.createComplaint(complaint);
            logger.info("Complaint created successfully - ID: {}", savedComplaint.getId());

            return new ResponseEntity<>(savedComplaint, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating complaint: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error filing complaint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error filing complaint: " + e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/complaints  – ADMIN gets all complaints
    // -------------------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComplaintResponseDTO>> getAllComplaints() {
        List<Complaint> complaints = complaintService.getAllComplaints();

        List<ComplaintResponseDTO> response = complaints.stream()
                .map(complaint -> new ComplaintResponseDTO(
                        complaint.getId(),
                        complaint.getTitle(),
                        complaint.getDescription(),
                        complaint.getCategory(),
                        complaint.getStatus() != null ? complaint.getStatus().name() : null,
                        complaint.getCreatedAt(),
                        complaint.getUser() != null ? complaint.getUser().getName() : "Unknown",
                        complaint.getUser() != null ? complaint.getUser().getEmail() : "Unknown"
                ))
                .toList();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // GET /api/complaints/user  – STUDENT views their own complaints
    // -------------------------------------------------------------------------
    @GetMapping("/user")
    public ResponseEntity<?> getUserComplaints() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.info("GET /api/complaints/user - Authentication: {}", authentication);
            logger.info("Is Authenticated: {}", authentication != null && authentication.isAuthenticated());
            if (authentication != null) {
                logger.info("Authorities: {}", authentication.getAuthorities());
            }

            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Request has no valid authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof CustomUserDetails)) {
                logger.warn("Invalid principal type: {}", principal.getClass().getSimpleName());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            CustomUserDetails userDetails = (CustomUserDetails) principal;
            User user = userDetails.getUser();

            logger.info("User {} attempting to fetch their complaints, Role: {}", user.getEmail(), user.getRole());
            logger.info("User authorities: {}", userDetails.getAuthorities());

            if (!user.getRole().toString().equals("STUDENT")) {
                logger.warn("User {} tried to access student complaints endpoint with role {}", user.getEmail(), user.getRole());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            logger.info("User fetching their complaints: {} (ID: {}, Role: {})", user.getEmail(), user.getId(), user.getRole());

            List<ComplaintResponseDTO> complaints = complaintService.getComplaintsByUser(user)
                    .stream()
                    .map(c -> new ComplaintResponseDTO(
                            c.getId(),
                            c.getTitle(),
                            c.getDescription(),
                            c.getCategory(),
                            c.getStatus() != null ? c.getStatus().name() : "UNKNOWN",
                            c.getCreatedAt(),
                            user.getName(),
                            user.getEmail()
                    ))
                    .toList();

            return new ResponseEntity<>(complaints, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getUserComplaints", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load complaints: " + e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/complaints/{id}  – view a single complaint
    // -------------------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaintById(@PathVariable("id") Long id) {
        Optional<Complaint> complaint = complaintService.getComplaintById(id);
        return complaint.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/complaints/{id}/status  – ADMIN updates complaint status
    // -------------------------------------------------------------------------
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateComplaintStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody StatusUpdateRequest request) {

        logger.info("PATCH /api/complaints/{}/status - requested status: {}", id, request.getStatus());

        ComplaintStatus newStatus = request.getStatus();
        Complaint updated = complaintService.updateComplaintStatus(id, newStatus);

        ComplaintResponseDTO response = new ComplaintResponseDTO(
                updated.getId(),
                updated.getTitle(),
                updated.getDescription(),
                updated.getCategory(),
                updated.getStatus().name(),
                updated.getCreatedAt(),
                updated.getUser() != null ? updated.getUser().getName() : "Unknown",
                updated.getUser() != null ? updated.getUser().getEmail() : "Unknown"
        );

        logger.info("Complaint {} status successfully changed to {}", id, newStatus);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // PUT /api/complaints/{id}  – ADMIN full update (existing endpoint)
    // -------------------------------------------------------------------------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Complaint> updateComplaint(@PathVariable("id") Long id, @RequestBody Complaint complaintDetails) {
        Complaint updatedComplaint = complaintService.updateComplaint(id, complaintDetails);
        if (updatedComplaint != null) {
            return new ResponseEntity<>(updatedComplaint, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // DELETE /api/complaints/{id}  – ADMIN deletes a complaint
    // -------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteComplaint(@PathVariable("id") Long id) {
        if (complaintService.deleteComplaint(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
