package com.skillsharing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// notificacion del sistema - patron observer (semana 5)
// se genera automaticamente al invitar o responder invitaciones - us12/us13
@Entity
@Table(name = "notificacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificacion_id")
    private Long notificacionId;

    // usuario que recibe la notificacion
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // sesion relacionada con la notificacion (puede ser null si la sesion se elimina)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sesion_id")
    private SesionAprendizaje sesion;

    @Column(nullable = false, length = 300)
    private String mensaje;

    @Column(nullable = false)
    @Builder.Default
    private Boolean visto = false;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
