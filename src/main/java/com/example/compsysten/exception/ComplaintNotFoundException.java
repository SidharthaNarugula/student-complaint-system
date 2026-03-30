package com.example.compsysten.exception;

public class ComplaintNotFoundException extends RuntimeException {

    private final Long complaintId;

    public ComplaintNotFoundException(Long complaintId) {
        super("Complaint not found with ID: " + complaintId);
        this.complaintId = complaintId;
    }

    public Long getComplaintId() {
        return complaintId;
    }
}
