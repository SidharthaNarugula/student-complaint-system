package com.example.compsysten.dto;

import java.time.LocalDateTime;

public class ComplaintResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String status;
    private LocalDateTime createdAt;
    private String submittedByName;
    private String submittedByEmail;

    public ComplaintResponseDTO() {
    }

    public ComplaintResponseDTO(Long id, String title, String description, String category,
                                String status, LocalDateTime createdAt,
                                String submittedByName, String submittedByEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.submittedByName = submittedByName;
        this.submittedByEmail = submittedByEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSubmittedByName() {
        return submittedByName;
    }

    public void setSubmittedByName(String submittedByName) {
        this.submittedByName = submittedByName;
    }

    public String getSubmittedByEmail() {
        return submittedByEmail;
    }

    public void setSubmittedByEmail(String submittedByEmail) {
        this.submittedByEmail = submittedByEmail;
    }
}