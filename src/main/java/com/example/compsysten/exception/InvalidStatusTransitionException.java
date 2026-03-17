package com.example.compsysten.exception;

import com.example.compsysten.model.ComplaintStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    private final ComplaintStatus currentStatus;
    private final ComplaintStatus requestedStatus;

    public InvalidStatusTransitionException(ComplaintStatus currentStatus, ComplaintStatus requestedStatus) {
        super(String.format(
                "Invalid status transition: cannot move from '%s' to '%s'. " +
                "Allowed transitions: %s → %s.",
                currentStatus.name(),
                requestedStatus.name(),
                currentStatus.name(),
                currentStatus.allowedNextStates().isEmpty()
                        ? "none (terminal state)"
                        : currentStatus.allowedNextStates().toString()
        ));
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    public ComplaintStatus getCurrentStatus() {
        return currentStatus;
    }

    public ComplaintStatus getRequestedStatus() {
        return requestedStatus;
    }
}
