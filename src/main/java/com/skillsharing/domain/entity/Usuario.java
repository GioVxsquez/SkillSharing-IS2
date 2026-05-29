package com.skillsharing.domain.entity;
import com.skillsharing.domain.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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
    @Column(nullable = false)
    private String password;
    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil;
    // hu22: el usuario tiene un conjunto de habilidades en su perfil
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_habilidad",
        joinColumns        = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "habilidad_id")
    )
    private Set<Habilidad> habilidades = new HashSet<>();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol = RolUsuario.APRENDIZ;
    @Column(nullable = false)
    private Boolean activo = true;
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
