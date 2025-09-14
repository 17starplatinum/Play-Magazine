package com.example.pmnotification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractNotification {

    @JsonProperty("text")
    private String text;

    @JsonProperty("addressee")
    private String addressee;
}