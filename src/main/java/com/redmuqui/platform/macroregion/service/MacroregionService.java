package com.redmuqui.platform.macroregion.service;

import com.redmuqui.platform.macroregion.dto.MacroregionCreateDTO;
import com.redmuqui.platform.macroregion.dto.MacroregionResponseDTO;
import com.redmuqui.platform.macroregion.dto.MacroregionUpdateDTO;

import java.util.List;

public interface MacroregionService {
    List<MacroregionResponseDTO> listar();
    MacroregionResponseDTO obtener(Long id);
    MacroregionResponseDTO crear(MacroregionCreateDTO dto);
    MacroregionResponseDTO actualizar(Long id, MacroregionUpdateDTO dto);
    void eliminar(Long id);
}
