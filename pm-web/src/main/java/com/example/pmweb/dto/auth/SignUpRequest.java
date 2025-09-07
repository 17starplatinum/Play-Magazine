package com.example.pmweb.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignUpRequest {
    @Size(min = 2, max = 32, message = "Имя пользователя должно содержать от 2 до 32 символов")
    @NotBlank(message = "Имя пользователя не может быть пустыми")
    private String name;

    @Size(min = 2, max = 32, message = "Фамилия пользователя должно содержать от 2 до 32 символов")
    @NotBlank(message = "Фамилия пользователя не может быть пустыми")
    private String surname;

    @Size(max = 64, message = "Длина пароля должна быть не более 64 символов")
    private String password;

    @Size(min = 5, max = 32, message = "Адрес электронной почты должен содержать от 5 до 32 символов")
    @NotBlank(message = "Адрес электронной почты не может быть пустыми")
    @Email(message = "Email адрес должен быть в формате user@example.com")
    private String email;
}
