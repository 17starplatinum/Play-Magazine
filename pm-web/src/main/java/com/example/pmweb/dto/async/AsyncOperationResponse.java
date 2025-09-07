package com.example.pmweb.dto.async;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AsyncOperationResponse {
    private String operationId;
    private OperationStatus status;
    private String message;
}

