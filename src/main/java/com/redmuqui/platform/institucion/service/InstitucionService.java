package com.redmuqui.platform.institucion.service;

import com.redmuqui.platform.institucion.dto.InstitucionCreateDTO;
import com.redmuqui.platform.institucion.dto.InstitucionResponseDTO;
import com.redmuqui.platform.institucion.dto.InstitucionUpdateDTO;

import java.util.List;

public interface InstitucionService {
    List<InstitucionResponseDTO> listar();
    InstitucionResponseDTO obtener(Long id);
    InstitucionResponseDTO crear(InstitucionCreateDTO dto);
    InstitucionResponseDTO actualizar(Long id, InstitucionUpdateDTO dto);
    void eliminar(Long id);
}
