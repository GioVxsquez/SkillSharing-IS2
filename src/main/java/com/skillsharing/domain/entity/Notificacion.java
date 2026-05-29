package com.skillsharing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// patron observer (semana 5 - patrones de comportamiento)
// las notificaciones son el resultado de los eventos disparados por el observer
// cuando una sesion cambia de estado, el observador crea una notificacion para cada inscrito
@Entity
@Table(name = "notificacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificacion_id")
    private Long notificacionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // puede ser null si la sesion fue eliminada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id")
    private SesionAprendizaje sesion;

    @Column(nullable = false, length = 300)
    private String mensaje;

    @Column(nullable = false)
    private Boolean visto = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
