package com.example.pmcore.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmailMessageDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String to;
    private String subject;
    private String text;
}