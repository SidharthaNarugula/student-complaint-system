package com.example.compsysten.dto;

import com.example.compsysten.model.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Status must not be null")
    private ComplaintStatus status;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(ComplaintStatus status) {
        this.status = status;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }
}
