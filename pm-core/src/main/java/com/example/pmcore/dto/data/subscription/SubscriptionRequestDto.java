package com.example.pmcore.dto.data.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequestDto {

    @NotNull
    private UUID id;

    @NotNull
    private UUID appId;

    @NotNull
    private UUID cardId;
}
