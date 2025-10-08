package com.vahabvahabov.SilentSignals.model.contact;

import lombok.Getter;

@Getter
public enum ContactType {
    FAMILY("Family"),
    FRIEND("Friend"),
    EMERGENCY_CONTACT("Emergency Contact"),
    NEIGHBOR("Neighbor"),
    COLLEAGUE("Colleague");

    private final String displayName;

    ContactType(String displayName) {
        this.displayName = displayName;
    }
}
