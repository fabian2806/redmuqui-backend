package com.redmuqui.platform.ejetematico.service;

import com.redmuqui.platform.ejetematico.dto.EjeTematicoCreateDTO;
import com.redmuqui.platform.ejetematico.dto.EjeTematicoResponseDTO;
import com.redmuqui.platform.ejetematico.dto.EjeTematicoUpdateDTO;

import java.util.List;

public interface EjeTematicoService {
    List<EjeTematicoResponseDTO> listar();
    EjeTematicoResponseDTO obtener(Long id);
    EjeTematicoResponseDTO crear(EjeTematicoCreateDTO dto);
    EjeTematicoResponseDTO actualizar(Long id, EjeTematicoUpdateDTO dto);
    void eliminar(Long id);
}
