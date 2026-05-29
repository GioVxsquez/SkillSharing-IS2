package com.skillsharing.application.service;
import com.skillsharing.application.strategy.BusquedaStrategy;
import com.skillsharing.domain.entity.SesionAprendizaje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.List;
// hu28: servicio del buscador que usa patron strategy (semana 5 - patrones de comportamiento)
@Service
public class BuscadorService {
    private final BusquedaStrategy busquedaPorHabilidad;
    private final BusquedaStrategy busquedaPorTitulo;
    @Autowired
    public BuscadorService(
            @Qualifier("busquedaPorHabilidad") BusquedaStrategy busquedaPorHabilidad,
            @Qualifier("busquedaPorTitulo") BusquedaStrategy busquedaPorTitulo) {
        this.busquedaPorHabilidad = busquedaPorHabilidad;
        this.busquedaPorTitulo = busquedaPorTitulo;
    }
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
