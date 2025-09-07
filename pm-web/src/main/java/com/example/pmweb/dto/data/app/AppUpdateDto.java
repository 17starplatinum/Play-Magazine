package com.example.pmweb.dto.data.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
@Getter
@Setter
public class AppUpdateDto implements Serializable {
    @NotNull
    @NotBlank
    private String newVersion;
    private String releaseNotes;

    private MultipartFile file;
}
