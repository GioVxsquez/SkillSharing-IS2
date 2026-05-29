package com.skillsharing.application.strategy;

import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.infrastructure.repository.SesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

// estrategia concreta de busqueda por titulo (semana 5 - patrones de comportamiento)
// busca sesiones activas que coincidan con el texto del titulo
// se usa cuando el usuario no filtra por habilidad sino por nombre de la sesion
@Component("busquedaPorTitulo")
@RequiredArgsConstructor
public class BusquedaPorTitulo implements BusquedaStrategy {

    private final SesionRepository sesionRepository;

    @Override
    public List<SesionAprendizaje> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            // si no hay criterio devuelve todas las sesiones activas
            return sesionRepository.findActivasByTitulo("");
        }
        return sesionRepository.findActivasByTitulo(criterio.trim());
    }
}
