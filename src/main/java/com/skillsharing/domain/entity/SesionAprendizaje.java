package com.skillsharing.domain.entity;
import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.domain.enums.ModalidadSesion;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "sesion_aprendizaje")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SesionAprendizaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sesion_id")
    private Long sesionId;
    @Column(nullable = false, length = 200)
    private String titulo;
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    @Column(name = "fecha_sesion", nullable = false)
    private LocalDateTime fechaSesion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModalidadSesion modalidad = ModalidadSesion.VIRTUAL;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSesion estado = EstadoSesion.PENDIENTE;
    @Column(name = "max_participantes", nullable = false)
    private Integer maxParticipantes = 20;
    @Column(name = "link_sesion", length = 500)
    private String linkSesion;
    @Column(length = 300)
    private String lugar;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Usuario instructor;
    // hu28: habilidad requerida para filtrar en el buscador
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habilidad_id")
    private Habilidad habilidadRequerida;
    // hu05: el admin solo puede aprobar si esto es true
    @Column(name = "material_cargado", nullable = false)
    private Boolean materialCargado = false;
    // materiales educativos asociados a esta sesion (hu05)
    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaterialEducativo> materiales = new ArrayList<>();
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
