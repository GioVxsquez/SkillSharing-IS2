package com.skillsharing.application.service;

import com.skillsharing.application.strategy.BusquedaStrategy;
import com.skillsharing.domain.entity.SesionAprendizaje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

// hu28: servicio del buscador que usa patron strategy (semana 5 - patrones de comportamiento)
//
// principio ocp (semana 2): el algoritmo de busqueda es intercambiable
// el cliente no cambia su logica si se decide usar otra estrategia
//
// relacion con diagrama de clases:
//   BuscadorService --> BusquedaStrategy
@Service
public class BuscadorService {

    private final BusquedaStrategy busquedaPorHabilidad;
    private final BusquedaStrategy busquedaPorTitulo;

    // inyeccion por nombre del bean (@Qualifier) para obtener las estrategias concretas
    @Autowired
    public BuscadorService(
            @Qualifier("busquedaPorHabilidad") BusquedaStrategy busquedaPorHabilidad,
            @Qualifier("busquedaPorTitulo") BusquedaStrategy busquedaPorTitulo) {
        this.busquedaPorHabilidad = busquedaPorHabilidad;
        this.busquedaPorTitulo = busquedaPorTitulo;
    }

    // delega la busqueda a la estrategia seleccionada (patron strategy)
    public List<SesionAprendizaje> buscar(String tipo, String criterio) {
        BusquedaStrategy estrategiaActiva;
        
        if ("habilidad".equalsIgnoreCase(tipo)) {
            estrategiaActiva = busquedaPorHabilidad;
        } else {
            estrategiaActiva = busquedaPorTitulo;
        }
        
        return estrategiaActiva.buscar(criterio);
    }
}
