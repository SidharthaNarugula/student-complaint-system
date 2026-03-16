package com.example.compsysten.service;

import com.example.compsysten.model.Complaint;
import com.example.compsysten.model.User;
import com.example.compsysten.repository.ComplaintRepository;
import com.example.compsysten.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ComplaintService {

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
        // Validate that a user is set
        if (complaint.getUser() == null) {
            throw new IllegalArgumentException("Complaint must be associated with a user");
        }

        // Validate that the user exists in the database
        if (complaint.getUser().getId() == null || complaint.getUser().getId() <= 0) {
            throw new IllegalArgumentException("Invalid user ID: user must be a valid persisted entity");
        }

        Optional<User> userOptional = userRepository.findById(complaint.getUser().getId());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + complaint.getUser().getId());
        }

        // Ensure we use the fully loaded user object from the database
        complaint.setUser(userOptional.get());

        complaint.setStatus("SUBMITTED");
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        return complaintRepository.save(complaint);
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
