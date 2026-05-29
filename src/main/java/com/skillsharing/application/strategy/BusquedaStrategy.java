package com.skillsharing.application.strategy;

import com.skillsharing.domain.entity.SesionAprendizaje;
import java.util.List;

// interfaz de estrategia de busqueda (semana 5 - patrones de comportamiento)
// hu28: el buscador puede cambiar su algoritmo sin modificar el cliente (BuscadorService)
//
// principio ocp (semana 2): el sistema esta abierto a agregar nuevas estrategias
// sin tocar el codigo existente del buscador
//
// principio isp (semana 2): interfaz pequena con un solo metodo
//
// relacion con diagrama de clases:
//   BusquedaStrategy <|-- BusquedaPorHabilidad
//   BusquedaStrategy <|-- BusquedaPorFecha
//   BuscadorService  --> BusquedaStrategy (usa la estrategia activa)
public interface BusquedaStrategy {
    List<SesionAprendizaje> buscar(String criterio);
}
