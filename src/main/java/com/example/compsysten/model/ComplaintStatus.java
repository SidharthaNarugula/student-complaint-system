package com.example.compsysten.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Represents the lifecycle states of a complaint.
 *
 * Allowed transitions:
 *   OPEN → IN_PROGRESS
 *   IN_PROGRESS → RESOLVED
 *   RESOLVED → CLOSED
 *
 * Backward transitions and state-skipping are prohibited.
 */
public enum ComplaintStatus {

    OPEN {
        @Override
        public Set<ComplaintStatus> allowedNextStates() {
            return EnumSet.of(IN_PROGRESS);
        }
    },
    IN_PROGRESS {
        @Override
        public Set<ComplaintStatus> allowedNextStates() {
            return EnumSet.of(RESOLVED);
        }
    },
    RESOLVED {
        @Override
        public Set<ComplaintStatus> allowedNextStates() {
            return EnumSet.of(CLOSED);
        }
    },
    CLOSED {
        @Override
        public Set<ComplaintStatus> allowedNextStates() {
            return EnumSet.noneOf(ComplaintStatus.class);
        }
    };

    /**
     * Returns the set of valid next states from the current state.
     */
    public abstract Set<ComplaintStatus> allowedNextStates();

    /**
     * Returns true if transitioning to {@code next} is a valid move.
     */
    public boolean canTransitionTo(ComplaintStatus next) {
        return allowedNextStates().contains(next);
    }
}
