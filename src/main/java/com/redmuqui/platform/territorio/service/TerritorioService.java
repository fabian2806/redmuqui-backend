package com.redmuqui.platform.territorio.service;

import com.redmuqui.platform.territorio.dto.TerritorioCreateDTO;
import com.redmuqui.platform.territorio.dto.TerritorioResponseDTO;
import com.redmuqui.platform.territorio.dto.TerritorioUpdateDTO;

import java.util.List;

public interface TerritorioService {
    List<TerritorioResponseDTO> listar();

    TerritorioResponseDTO obtener(Long id);

    TerritorioResponseDTO crear(TerritorioCreateDTO dto);

    TerritorioResponseDTO actualizar(Long id, TerritorioUpdateDTO dto);

    void eliminar(Long id);
}
