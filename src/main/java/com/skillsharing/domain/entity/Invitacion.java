package com.skillsharing.domain.entity;
import com.skillsharing.domain.enums.EstadoInvitacion;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
// hu06: entidad para gestionar invitaciones privadas a sesiones
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
    private Long invitacionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionAprendizaje sesion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitado_id", nullable = false)
    private Usuario invitado;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInvitacion estado;
    @Column(nullable = false)
    private LocalDateTime fechaEnvio;
}
