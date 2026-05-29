package com.skillsharing.application.strategy;

import com.skillsharing.domain.entity.SesionAprendizaje;
import com.skillsharing.infrastructure.repository.SesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

// estrategia concreta de busqueda por habilidad (semana 5 - patrones de comportamiento)
// hu28: el usuario filtra sesiones por la habilidad que quiere aprender
//
// herencia e implementacion (poo semana 1): implementa BusquedaStrategy
// polimorfismo (poo semana 1): el cliente usa BusquedaStrategy, no sabe que es esta clase
// principio dip (semana 2): depende de SesionRepository (interfaz), no de hibernate directamente
@Component("busquedaPorHabilidad")
@RequiredArgsConstructor
public class BusquedaPorHabilidad implements BusquedaStrategy {

    private final SesionRepository sesionRepository;

    // busca sesiones activas cuya habilidad requerida coincide con el criterio
    // usa busqueda parcial (like) para mayor flexibilidad del usuario
    @Override
    public List<SesionAprendizaje> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return sesionRepository.findActivasByHabilidad("");
        }
        return sesionRepository.findActivasByHabilidad(criterio.trim());
    }
}
