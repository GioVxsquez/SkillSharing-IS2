package com.skillsharing.application.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class UsuarioRegistroDto {
    @NotBlank(message = "el nombre es obligatorio")
    @Size(min = 2, max = 100, message = "el nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    @NotBlank(message = "el email es obligatorio")
    @Email(message = "formato de email invalido")
    private String email;
    @NotBlank(message = "la contrasena es obligatoria")
    @Size(min = 6, message = "la contrasena debe tener al menos 6 caracteres")
    private String password;
    private String rol;
}
