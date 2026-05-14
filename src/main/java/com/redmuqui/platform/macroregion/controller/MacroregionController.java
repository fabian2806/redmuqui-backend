package com.redmuqui.platform.macroregion.controller;

import com.redmuqui.platform.common.catalog.controller.BaseCatalogoController;
import com.redmuqui.platform.macroregion.dto.MacroregionCreateDTO;
import com.redmuqui.platform.macroregion.dto.MacroregionResponseDTO;
import com.redmuqui.platform.macroregion.dto.MacroregionUpdateDTO;
import com.redmuqui.platform.macroregion.service.MacroregionService;
import com.redmuqui.platform.macroregion.service.MacroregionServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/macroregiones")
@Tag(name = "Macroregiones", description = "Catálogo de macroregiones")
public class MacroregionController extends BaseCatalogoController<MacroregionResponseDTO> {

    private final MacroregionService service;

    public MacroregionController(MacroregionServiceImpl service) {
        super(service);
        this.service = service;
    }

    @Override
    protected String getRutaBase() {
        return "/api/v1/macroregiones";
    }

}

