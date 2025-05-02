package com.example.backend.dto.auth.rolestatus;

import org.springframework.stereotype.Component;

@Component("approved")
public class ApprovedStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Ваша заявка была одобрена";
    }
}
