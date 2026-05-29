package com.skillsharing.application.service;
import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.domain.entity.Usuario;
import com.skillsharing.infrastructure.repository.HabilidadRepository;
import com.skillsharing.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final HabilidadRepository habilidadRepository;
    // hu22: el usuario puede actualizar su perfil anadiendo habilidades
    @Transactional
    public Usuario actualizarHabilidades(Long usuarioId, List<Long> habilidadesIds) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
        List<Habilidad> nuevasHabilidades = habilidadRepository.findAllById(habilidadesIds);
        usuario.getHabilidades().clear();
        usuario.getHabilidades().addAll(nuevasHabilidades);
        return usuarioRepository.save(usuario);
    }
    public Usuario buscarPorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));
    }
}
