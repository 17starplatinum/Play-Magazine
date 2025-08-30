package com.example.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EditProfileRequest {
    @Id
    @UuidGenerator
    private String email;

    @Size(min = 2, max = 32, message = "Имя пользователя должно содержать от 2 до 32 символов")
    @NotBlank(message = "Имя пользователя не может быть пустыми")
    private String name;

    @Size(min = 2, max = 32, message = "Фамилия пользователя должно содержать от 2 до 32 символов")
    @NotBlank(message = "Фамилия пользователя не может быть пустыми")
    private String surname;

    @Size(max = 64, message = "Длина пароля должна быть не более 64 символов")
    private String password;

    @DateTimeFormat
    private LocalDate birthday;

    @Size(max = 64, message = "Длина пароля должна быть не более 64 символов")
    private String newPassword;
}
