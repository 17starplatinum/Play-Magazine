package com.example.backend.dto.data.app;

import com.example.backend.dto.data.subscription.SubscriptionCreationDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppCreateRequest implements Serializable {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Double price;

    @NotNull
    private AppType type;

    @NotBlank
    private String subscriptionName;

    private Double subscriptionPrice;

    private Integer subscriptionDays;

    private Boolean autoRenewal;

    private MultipartFile file;

    @Positive
    private Integer minRamMb;

    @Positive
    private Integer minStorageMb;

    @NotEmpty
    private List<String> compatibleOs;

    public enum AppType {
        FREE, PAID, SUBSCRIPTION
    }
}