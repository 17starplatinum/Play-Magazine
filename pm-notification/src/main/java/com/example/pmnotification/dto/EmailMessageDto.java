package com.example.pmnotification.dto;

import lombok.Data;

@Data
public class EmailMessageDto {
    private String to;
    private String subject;
    private String text;
}