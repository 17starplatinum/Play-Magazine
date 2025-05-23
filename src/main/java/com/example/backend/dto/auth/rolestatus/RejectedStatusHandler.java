package com.example.backend.dto.auth.rolestatus;

import org.springframework.stereotype.Component;

@Component("rejected")
public class RejectedStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Ваша заявка была отклонена";
    }
}
