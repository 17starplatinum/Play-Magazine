package com.example.pmcore.jca;

import javax.resource.spi.ConnectionRequestInfo;

public record BitrixConnectionRequestInfo(String token) implements ConnectionRequestInfo {
    public String getToken() {
        return token;
    }
}