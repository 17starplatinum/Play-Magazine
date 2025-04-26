package com.example.backend.dto.auth.rolestatus;

import org.springframework.stereotype.Component;

@Component("none")
public class NoneStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Вы не отправили заявку";
    }
}
