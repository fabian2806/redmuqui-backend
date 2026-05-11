package com.redmuqui.platform.institucion.controller;

import com.redmuqui.platform.common.catalog.controller.BaseCatalogoController;
import com.redmuqui.platform.institucion.dto.InstitucionResponseDTO;
import com.redmuqui.platform.institucion.service.InstitucionService;
import com.redmuqui.platform.institucion.service.InstitucionServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/instituciones")
@Tag(name = "Instituciones", description = "Catálogo de instituciones miembro")
public class InstitucionController extends BaseCatalogoController<InstitucionResponseDTO> {

    private final InstitucionService service;

    public InstitucionController(InstitucionServiceImpl service) {
        super(service);
        this.service = service;
    }

    @Override
    protected String getRutaBase() {
        return "/api/v1/instituciones";
    }
}
