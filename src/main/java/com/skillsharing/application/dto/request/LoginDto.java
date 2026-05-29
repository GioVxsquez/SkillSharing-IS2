package com.skillsharing.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// dto de login - solo email y password
@Data
public class LoginDto {

    @NotBlank(message = "el email es obligatorio")
    @Email(message = "formato de email invalido")
    private String email;

    @NotBlank(message = "la contrasena es obligatoria")
    private String password;
}
