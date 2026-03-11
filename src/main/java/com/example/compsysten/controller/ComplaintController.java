package com.example.compsysten.controller;

import com.example.compsysten.model.Complaint;
import com.example.compsysten.model.User;
import com.example.compsysten.security.CustomUserDetails;
import com.example.compsysten.service.ComplaintService;
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

    @PostMapping
    public ResponseEntity<?> createComplaint(@RequestBody Complaint complaint) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            logger.info("POST /api/complaints - Authentication: {}", authentication);
            logger.info("Is Authenticated: {}", authentication != null && authentication.isAuthenticated());

            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Authentication failed - returning 401");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Not authenticated"));
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

            logger.info("User authenticated: {} (ID: {})", user.getEmail(), user.getId());
            logger.info("User role: {}", user.getRole());

            complaint.setUser(user);
            Complaint savedComplaint = complaintService.createComplaint(complaint);
            logger.info("Complaint created successfully - ID: {}", savedComplaint.getId());

            return new ResponseEntity<>(savedComplaint, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error filing complaint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error filing complaint: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        List<Complaint> complaints = complaintService.getAllComplaints();
        return new ResponseEntity<>(complaints, HttpStatus.OK);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Complaint>> getUserComplaints() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        List<Complaint> complaints = complaintService.getComplaintsByUser(user);
        return new ResponseEntity<>(complaints, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaintById(@PathVariable Long id) {
        Optional<Complaint> complaint = complaintService.getComplaintById(id);
        return complaint.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Complaint> updateComplaint(@PathVariable Long id, @RequestBody Complaint complaintDetails) {
        Complaint updatedComplaint = complaintService.updateComplaint(id, complaintDetails);
        if (updatedComplaint != null) {
            return new ResponseEntity<>(updatedComplaint, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteComplaint(@PathVariable Long id) {
        if (complaintService.deleteComplaint(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
