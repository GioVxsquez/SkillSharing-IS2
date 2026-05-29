package com.skillsharing.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// inscripcion reemplaza a participacion del myevent original
// un usuario se inscribe a una sesion activa en rol de aprendiz
// hu09: logica de inscripcion en inscripcionservice
@Entity
@Table(name = "inscripcion",
       uniqueConstraints = @UniqueConstraint(columnNames = {"sesion_id", "usuario_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inscripcion_id")
    private Long inscripcionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionAprendizaje sesion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // rol del participante en la sesion (APRENDIZ por defecto)
    @Column(name = "rol_sesion", length = 20, nullable = false)
    private String rolSesion = "APRENDIZ";

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion = LocalDateTime.now();
}
