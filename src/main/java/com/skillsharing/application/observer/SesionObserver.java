package com.skillsharing.application.observer;

import com.skillsharing.domain.entity.SesionAprendizaje;

// interfaz del patron observer (semana 5 - patrones de comportamiento)
// define el contrato que deben cumplir todos los observadores de sesiones
// principio isp (semana 2): interfaz pequena y especifica, no un contrato enorme
// principio dip (semana 2): los servicios dependen de esta interfaz, no de implementaciones concretas
//
// relacion con diagrama de clases:
//   SesionObserver <|-- NotificacionObserver
//   SesionFacade --> SesionObserver (usa la lista de observadores)
public interface SesionObserver {
    void onSesionActualizada(SesionAprendizaje sesion, String evento);
}
