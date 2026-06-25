package com.redmuqui.platform.config.controller;

import com.redmuqui.platform.config.dto.ConfiguracionSistemaDTO;
import com.redmuqui.platform.config.service.ConfiguracionSistemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/configuracion")
@RequiredArgsConstructor
public class ConfiguracionSistemaController {

    private final ConfiguracionSistemaService service;

    @GetMapping
    public ConfiguracionSistemaDTO obtenerConfiguracion() {
        return service.obtenerConfiguracion();
    }

    @PutMapping
    public ConfiguracionSistemaDTO actualizarConfiguracion(
            @RequestBody ConfiguracionSistemaDTO dto
    ) {
        return service.actualizarConfiguracion(dto);
    }
}