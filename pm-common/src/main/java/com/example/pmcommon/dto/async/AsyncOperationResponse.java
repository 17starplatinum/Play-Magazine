package com.example.pmcommon.dto.async;

import com.example.pmcommon.model.OperationStatus;
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

