package com.skillsharing.application.service;

import com.skillsharing.domain.entity.Habilidad;
import com.skillsharing.infrastructure.repository.HabilidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HabilidadService {

    private final HabilidadRepository habilidadRepository;

    public List<Habilidad> listarTodas() {
        return habilidadRepository.findAll();
    }
}
