package com.skillsharing.domain.entity;

import com.skillsharing.domain.enums.EstadoInvitacion;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// hu06: entidad para gestionar invitaciones privadas a sesiones
// hu07: guarda si la invitacion fue aceptada o rechazada
// hu28: permite listar invitaciones pendientes del usuario
@Entity
@Table(name = "invitaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitacion_id")
    private Long invitacionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionAprendizaje sesion;

    // a quien se le envia la invitacion (el aprendiz)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "invitado_id", nullable = false)
    private Usuario invitado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoInvitacion estado = EstadoInvitacion.PENDIENTE;

    @Column(name = "fecha_envio", nullable = false)
    @Builder.Default
    private LocalDateTime fechaEnvio = LocalDateTime.now();
}
