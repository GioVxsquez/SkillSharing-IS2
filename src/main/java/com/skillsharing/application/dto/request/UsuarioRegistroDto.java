package com.skillsharing.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// dto de registro - separa la capa de presentacion del dominio
// principio srp (semana 2): este objeto solo transporta datos de entrada, sin logica
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

    // rol opcional, por defecto sera APRENDIZ si no se especifica
    private String rol;
}
