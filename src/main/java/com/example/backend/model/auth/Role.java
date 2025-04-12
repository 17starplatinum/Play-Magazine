package com.example.backend.model.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Role {
    ADMIN ("Admin", 4),
    MODERATOR ("Moderator", 3),
    DEVELOPER ("Developer", 2),
    USER ("User", 1),
    GUEST ("Guest", 0);

    @Getter
    private final String name;

    private final int value;

    @Override
    public String toString() {
        return getName();
    }

    public int compare(Role that) {
        return Integer.compare(this.value, that.value);
    }
}
