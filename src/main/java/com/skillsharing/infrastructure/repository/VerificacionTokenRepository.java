package com.skillsharing.infrastructure.repository;

import com.skillsharing.domain.entity.VerificacionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// repositorio para buscar tokens de verificacion de correo
@Repository
public interface VerificacionTokenRepository extends JpaRepository<VerificacionToken, Long> {

    // busca el token por su valor uuid (llega en la url del correo)
    Optional<VerificacionToken> findByToken(String token);
}
