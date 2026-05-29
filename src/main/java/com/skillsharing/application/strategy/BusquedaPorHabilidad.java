package com.skillsharing.application.strategy;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.infrastructure.repository.SesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
// hu28: el usuario filtra sesiones por la habilidad que quiere aprender
@Component("busquedaPorHabilidad")
@RequiredArgsConstructor
public class BusquedaPorHabilidad implements BusquedaStrategy {
    private final SesionRepository sesionRepository;
    @Override
    public List<SesionAprendizaje> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return sesionRepository.findActivasByHabilidad("");
        }
        return sesionRepository.findActivasByHabilidad(criterio.trim());
    }
}
