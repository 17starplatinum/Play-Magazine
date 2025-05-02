package com.example.backend.model.auth;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Role {
    ADMIN(4),
    MODERATOR(3),
    DEVELOPER(2),
    USER(1),
    GUEST(0);

    private final int value;

    @Override
    public String toString() {
        return name();
    }

    public int compare(Role that) {
        return Integer.compare(this.value, that.value);
    }
}
