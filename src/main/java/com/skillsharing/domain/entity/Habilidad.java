package com.skillsharing.domain.entity;

import jakarta.persistence.*;
import lombok.*;

// entidad de habilidad - us21/us22 gestion de habilidades
// permite clasificar sesiones y perfiles de instructor
@Entity
@Table(name = "habilidad")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Habilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "habilidad_id")
    private Long habilidadId;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;
}
