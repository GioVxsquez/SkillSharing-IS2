package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.HabilidadRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// us21/us22: gestion de habilidades y perfil del instructor
// principio srp: solo gestiona habilidades
@Service
@RequiredArgsConstructor
public class HabilidadService {

    private final HabilidadRepository habilidadRepository;
    private final UsuarioRepository usuarioRepository;

    // listar todas las habilidades disponibles
    public List<Habilidad> listarTodas() {
        return habilidadRepository.findAll();
    }

    // us21: actualizar habilidades del perfil del usuario
    @Transactional
    public Usuario actualizarHabilidades(Long usuarioId, List<Long> habilidadIds) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        Set<Habilidad> nuevasHabilidades = habilidadIds.stream()
                .map(id -> habilidadRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("habilidad no encontrada: " + id)))
                .collect(Collectors.toSet());

        usuario.getHabilidades().clear();
        usuario.getHabilidades().addAll(nuevasHabilidades);
        return usuarioRepository.save(usuario);
    }

    // us22: buscar instructores por habilidad
    public List<Usuario> buscarInstructoresPorHabilidad(String nombreHabilidad) {
        return usuarioRepository.findAll().stream()
                .filter(u -> "INSTRUCTOR".equals(u.getRol().name()))
                .filter(u -> u.getHabilidades().stream()
                        .anyMatch(h -> h.getNombre().equalsIgnoreCase(nombreHabilidad)))
                .collect(Collectors.toList());
    }
}
