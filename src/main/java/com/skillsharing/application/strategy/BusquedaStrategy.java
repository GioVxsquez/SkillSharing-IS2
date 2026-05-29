package com.skillsharing.application.strategy;
import com.skillsharing.domain.entity.SesionAprendizaje;
import java.util.List;
// hu28: el buscador puede cambiar su algoritmo sin modificar el cliente (BuscadorService)
public interface BusquedaStrategy {
    List<SesionAprendizaje> buscar(String criterio);
}
