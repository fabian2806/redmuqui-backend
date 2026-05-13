package com.redmuqui.platform.ejetematico.controller;

import com.redmuqui.platform.common.catalog.controller.BaseCatalogoController;
import com.redmuqui.platform.ejetematico.dto.EjeTematicoResponseDTO;
import com.redmuqui.platform.ejetematico.service.EjeTematicoService;
import com.redmuqui.platform.ejetematico.service.EjeTematicoServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ejes-tematicos")
@Tag(name = "Ejes Temáticos", description = "Catálogo de ejes temáticos")
public class EjeTematicoController extends BaseCatalogoController<EjeTematicoResponseDTO> {

    private final EjeTematicoService service;

    public EjeTematicoController(EjeTematicoServiceImpl service) {
        super(service);
        this.service = service;
    }

    @Override
    protected String getRutaBase() {
        return "/api/v1/ejes-tematicos";
    }
}
