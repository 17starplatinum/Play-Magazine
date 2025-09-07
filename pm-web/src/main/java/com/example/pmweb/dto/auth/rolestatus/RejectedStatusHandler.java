package com.example.pmweb.dto.auth.rolestatus;

import org.springframework.stereotype.Component;

@Component("REJECTED")
public class RejectedStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Ваша заявка была отклонена";
    }
}
