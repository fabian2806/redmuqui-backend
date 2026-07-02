package com.redmuqui.platform.estado.service;

import com.redmuqui.platform.estado.dto.EstadoCreateDTO;
import com.redmuqui.platform.estado.dto.EstadoResponseDTO;
import com.redmuqui.platform.estado.dto.EstadoUpdateDTO;
import com.redmuqui.platform.estado.entity.ModuloEstado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EstadoService {

    List<EstadoResponseDTO> listar(ModuloEstado modulo);

    Page<EstadoResponseDTO> listarPaginado(ModuloEstado modulo, Pageable pageable);

    EstadoResponseDTO obtener(Long id);

    EstadoResponseDTO crear(EstadoCreateDTO dto);

    EstadoResponseDTO actualizar(Long id, EstadoUpdateDTO dto);

    void eliminar(Long id);
}
