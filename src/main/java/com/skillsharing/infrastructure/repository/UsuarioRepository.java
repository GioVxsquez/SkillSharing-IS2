package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// principio dip (semana 2): el servicio depende de esta interfaz, no de la implementacion concreta
// spring data jpa genera la implementacion en runtime automaticamente
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    java.util.List<Usuario> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(String nombre, String email);
}
