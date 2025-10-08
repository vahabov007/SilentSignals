package com.vahabvahabov.SilentSignals.model.alert;

import lombok.Getter;

@Getter
public enum AlertStatus {
    ACTIVE("Active"),
    RESOLVED("Resolved"),
    ESCALATED("Escalated"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled");

    private final String displayName;

    AlertStatus(String displayName) {
        this.displayName = displayName;
    }

}