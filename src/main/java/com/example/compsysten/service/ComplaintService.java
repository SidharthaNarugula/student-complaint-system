package com.example.compsysten.service;

import com.example.compsysten.exception.ComplaintNotFoundException;
import com.example.compsysten.exception.InvalidStatusTransitionException;
import com.example.compsysten.model.Complaint;
import com.example.compsysten.model.ComplaintStatus;
import com.example.compsysten.model.User;
import com.example.compsysten.repository.ComplaintRepository;
import com.example.compsysten.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ComplaintService {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintService.class);

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public ComplaintService(ComplaintRepository complaintRepository, UserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public List<Complaint> getComplaintsByUser(User user) {
        return complaintRepository.findByUser(user);
    }

    public Optional<Complaint> getComplaintById(Long id) {
        return complaintRepository.findById(id);
    }

    public Complaint createComplaint(Complaint complaint) {
        if (complaint.getUser() == null) {
            throw new IllegalArgumentException("Complaint must be associated with a user");
        }

        if (complaint.getUser().getId() == null || complaint.getUser().getId() <= 0) {
            throw new IllegalArgumentException("Invalid user ID: user must be a valid persisted entity");
        }

        Optional<User> userOptional = userRepository.findById(complaint.getUser().getId());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + complaint.getUser().getId());
        }

        complaint.setUser(userOptional.get());
        complaint.setStatus(ComplaintStatus.OPEN);
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());

        Complaint saved = complaintRepository.save(complaint);
        logger.info("Complaint created: id={}, title='{}', user={}", saved.getId(), saved.getTitle(),
                saved.getUser().getEmail());
        return saved;
    }

    /**
     * Transitions a complaint to the requested status, enforcing the state machine rules.
     * Only valid forward transitions are allowed.
     *
     * @param id        complaint to update
     * @param newStatus the desired next status
     * @return the updated, persisted complaint
     * @throws ComplaintNotFoundException       if no complaint exists with the given id
     * @throws InvalidStatusTransitionException if the transition is not permitted
     */
    public Complaint updateComplaintStatus(Long id, ComplaintStatus newStatus) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Status update failed – complaint not found: id={}", id);
                    return new ComplaintNotFoundException(id);
                });

        ComplaintStatus current = complaint.getStatus();

        if (!current.canTransitionTo(newStatus)) {
            logger.warn("Invalid status transition for complaint id={}: {} -> {} (allowed: {})",
                    id, current, newStatus, current.allowedNextStates());
            throw new InvalidStatusTransitionException(current, newStatus);
        }

        complaint.setStatus(newStatus);
        complaint.setUpdatedAt(LocalDateTime.now());
        Complaint updated = complaintRepository.save(complaint);

        logger.info("Complaint status updated: id={}, {} -> {}", id, current, newStatus);
        return updated;
    }

    public Complaint updateComplaint(Long id, Complaint complaintDetails) {
        Optional<Complaint> complaintOptional = complaintRepository.findById(id);
        if (complaintOptional.isPresent()) {
            Complaint complaint = complaintOptional.get();
            complaint.setTitle(complaintDetails.getTitle());
            complaint.setDescription(complaintDetails.getDescription());
            complaint.setCategory(complaintDetails.getCategory());
            complaint.setStatus(complaintDetails.getStatus());
            complaint.setUpdatedAt(LocalDateTime.now());
            return complaintRepository.save(complaint);
        }
        return null;
    }

    public boolean deleteComplaint(Long id) {
        if (complaintRepository.existsById(id)) {
            complaintRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
