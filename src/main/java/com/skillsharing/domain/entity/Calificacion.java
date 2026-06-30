package com.skillsharing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// calificacion de una sesion por un aprendiz - us19/us20
// solo se puede calificar si el usuario estuvo inscrito y la sesion finalizo
@Entity
@Table(name = "calificacion",
       uniqueConstraints = @UniqueConstraint(columnNames = {"sesion_id", "usuario_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calificacion_id")
    private Long calificacionId;

    // sesion que se califica
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionAprendizaje sesion;

    // aprendiz que califica
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // puntuacion de 1 a 5 estrellas
    @Column(nullable = false)
    private Integer puntuacion;

    @Column(length = 500)
    private String comentario;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
