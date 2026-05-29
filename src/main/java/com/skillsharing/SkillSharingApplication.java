package com.skillsharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// punto de entrada de la aplicacion
// spring arranca el contexto y registra todos los beans automaticamente
// patron singleton (semana 3): spring gestiona una sola instancia de cada servicio
// arquitectura de capas (semana 7): controller -> service -> repository -> entity
@SpringBootApplication
public class SkillSharingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillSharingApplication.class, args);
        System.out.println("skillsharing backend corriendo en http://localhost:8080");
        System.out.println("api disponible en /api/...");
    }
}
