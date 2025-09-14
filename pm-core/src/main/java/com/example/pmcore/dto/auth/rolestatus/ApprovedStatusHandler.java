package com.example.pmcore.dto.auth.rolestatus;

import org.springframework.stereotype.Component;

@Component("APPROVED")
public class ApprovedStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Ваша заявка была одобрена";
    }
}
