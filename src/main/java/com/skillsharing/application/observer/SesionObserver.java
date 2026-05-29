package com.skillsharing.application.observer;
import com.skillsharing.domain.entity.SesionAprendizaje;
public interface SesionObserver {
    void onSesionActualizada(SesionAprendizaje sesion, String evento);
}
