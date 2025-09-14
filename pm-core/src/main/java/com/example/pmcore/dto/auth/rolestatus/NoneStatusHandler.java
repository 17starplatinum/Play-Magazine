package com.example.pmcore.dto.auth.rolestatus;

import org.springframework.stereotype.Component;

@Component("NONE")
public class NoneStatusHandler implements AdminRequestStatusHandler {
    @Override
    public String getStatusMessage() {
        return "Вы не отправили заявку";
    }
}
