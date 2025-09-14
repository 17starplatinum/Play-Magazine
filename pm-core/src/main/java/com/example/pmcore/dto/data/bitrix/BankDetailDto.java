package com.example.pmcore.dto.data.bitrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BankDetailDto {
    @JsonProperty("ID")
    private String id;
    @JsonProperty("XML_ID")
    private String uuid;
    @JsonProperty("RQ_ACC_NUM")
    private String number;
    @JsonProperty("RQ_COR_ACC_NUM")
    private String expiredDate;
}
