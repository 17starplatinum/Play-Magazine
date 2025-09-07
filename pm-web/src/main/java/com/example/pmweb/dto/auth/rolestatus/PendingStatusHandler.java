package com.example.pmweb.dto.auth.rolestatus;

import org.springframework.stereotype.Component;

@Component("PENDING")
public class PendingStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Ваша заявка всё ещё находится в обработке";
    }
}
