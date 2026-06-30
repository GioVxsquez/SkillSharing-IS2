package com.skillsharing.domain.entity;

import com.skillsharing.domain.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// entidad principal del sistema
// principio srp (semana 2): solo representa los datos del usuario
// la logica de negocio va en usuarioservice, no aqui
// poo (semana 1): encapsulamiento de datos con lombok
@Entity
@Table(name = "usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // la password siempre se guarda encriptada con bcrypt
    @Column(nullable = false)
    private String password;

    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RolUsuario rol = RolUsuario.APRENDIZ;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // us21/us22: habilidades del perfil del instructor
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_habilidad",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "habilidad_id")
    )
    @Builder.Default
    private java.util.Set<Habilidad> habilidades = new java.util.HashSet<>();

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
