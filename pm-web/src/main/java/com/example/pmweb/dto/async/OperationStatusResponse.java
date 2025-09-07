package com.example.pmweb.dto.async;

import com.example.pmcommon.model.OperationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OperationStatusResponse {
    private String operationId;
    private OperationStatus status;
    private LocalDateTime lastUpdated;
    private String details;
}
