package com.example.backend.dto.auth.rolestatus;

import org.springframework.stereotype.Component;

@Component("pending")
public class PendingStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Ваша заявка всё ещё находится в обработке";
    }
}
