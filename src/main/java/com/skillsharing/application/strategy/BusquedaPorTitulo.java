package com.skillsharing.application.strategy;
import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.infrastructure.repository.SesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
@Component("busquedaPorTitulo")
@RequiredArgsConstructor
public class BusquedaPorTitulo implements BusquedaStrategy {
    private final SesionRepository sesionRepository;
    @Override
    public List<SesionAprendizaje> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return sesionRepository.findActivasByTitulo("");
        }
        return sesionRepository.findActivasByTitulo(criterio.trim());
    }
}
