package com.redmuqui.platform.territorio.controller;

import com.redmuqui.platform.common.catalog.controller.BaseCatalogoController;
import com.redmuqui.platform.territorio.dto.TerritorioResponseDTO;
import com.redmuqui.platform.territorio.service.TerritorioService;
import com.redmuqui.platform.territorio.service.TerritorioServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/territorios")
@Tag(name = "Territorios", description = "Catálogo de territorios")
public class TerritorioController extends BaseCatalogoController<TerritorioResponseDTO> {

    private final TerritorioService service;

    public TerritorioController(TerritorioServiceImpl service) {
        super(service);
        this.service = service;
    }

    @Override
    protected String getRutaBase() {
        return "/api/v1/territorios";
    }
}
