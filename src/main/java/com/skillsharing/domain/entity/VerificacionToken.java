package com.skillsharing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// entidad para el token de verificacion de correo electronico
// patron: cada usuario tiene un token unico generado al registrarse
// cuando el usuario hace clic en el enlace del correo, se valida este token
// y se activa su cuenta - HU29 (activar cuenta)
@Entity
@Table(name = "verificacion_token")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerificacionToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    // token uuid unico que se envia en el enlace del correo
    @Column(nullable = false, unique = true)
    private String token;

    // relacion con el usuario dueno del token
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // el token expira en 24 horas
    @Column(name = "fecha_expira", nullable = false)
    private LocalDateTime fechaExpira;

    // marca si el token ya fue utilizado para no permitir reuso
    @Column(nullable = false)
    @Builder.Default
    private Boolean usado = false;
}
