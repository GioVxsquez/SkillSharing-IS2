package com.skillsharing.domain.entity;

import com.skillsharing.domain.enums.EstadoSesion;
import com.skillsharing.domain.enums.ModalidadSesion;
import com.skillsharing.domain.enums.TipoSesion;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// sesionaprendizaje reemplaza a evento del proyecto myevent
// principio srp (semana 2): solo contiene datos, la logica va en sesionservice
// patron state (semana 5): el campo estado modela el ciclo de vida de la sesion
// poo (semana 1): herencia de los conceptos de entidad con jpa
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

    // virtual o presencial
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ModalidadSesion modalidad = ModalidadSesion.VIRTUAL;

    // patron state (semana 5): pendiente -> activa -> finalizada / rechazada
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoSesion estado = EstadoSesion.PENDIENTE;

    // hu01: define si la sesion aparece en el catalogo o solo por invitacion
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TipoSesion tipo = TipoSesion.PUBLICA;

    @Column(name = "max_participantes", nullable = false)
    @Builder.Default
    private Integer maxParticipantes = 20;

    // enlace de videollamada si la sesion es virtual
    @Column(name = "link_sesion", length = 500)
    private String linkSesion;

    // lugar fisico si la sesion es presencial
    @Column(length = 300)
    private String lugar;

    // quien crea y dirige la sesion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Usuario instructor;

    // hu28: habilidad requerida para filtrar en el buscador
    // reemplaza la categoria social del myevent original
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habilidad_id")
    private Habilidad habilidadRequerida;

    // hu05: el admin solo puede aprobar si esto es true
    // el instructor sube el material educativo -> se actualiza a true
    @Column(name = "material_cargado", nullable = false)
    @Builder.Default
    private Boolean materialCargado = false;

    // materiales educativos asociados a esta sesion (hu05)
    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MaterialEducativo> materiales = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
