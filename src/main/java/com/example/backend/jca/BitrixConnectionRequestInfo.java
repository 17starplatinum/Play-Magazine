package com.example.backend.jca;

import javax.resource.spi.ConnectionRequestInfo;

public class BitrixConnectionRequestInfo implements ConnectionRequestInfo {

    private final String token;

    public BitrixConnectionRequestInfo(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BitrixConnectionRequestInfo) {
            return this.token.equals(((BitrixConnectionRequestInfo) obj).token);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }
}