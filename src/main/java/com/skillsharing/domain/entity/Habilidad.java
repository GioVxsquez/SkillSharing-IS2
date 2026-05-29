package com.skillsharing.domain.entity;

import jakarta.persistence.*;
import lombok.*;

// habilidad reemplaza a categoria del proyecto myevent
// hu22: los usuarios agregan habilidades a su perfil (programacion, idiomas, cocina, etc)
// hu28: el buscador filtra sesiones por habilidad requerida
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
