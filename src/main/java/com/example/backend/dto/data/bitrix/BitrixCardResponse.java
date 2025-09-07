package com.example.backend.dto.data.bitrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BitrixCardResponse {
    @JsonProperty("result")
    private List<CardResponse> bankDetails;
}

