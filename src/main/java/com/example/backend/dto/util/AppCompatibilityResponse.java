package com.example.backend.dto.util;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppCompatibilityResponse {
    private boolean compatible;
    private List<String> issues;
}
