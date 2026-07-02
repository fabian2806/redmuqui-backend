package com.redmuqui.platform.moneda.service;

import com.redmuqui.platform.moneda.dto.MonedaCreateDTO;
import com.redmuqui.platform.moneda.dto.MonedaResponseDTO;
import com.redmuqui.platform.moneda.dto.MonedaUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MonedaService {

    List<MonedaResponseDTO> listar();

    Page<MonedaResponseDTO> listarPaginado(Pageable pageable);

    MonedaResponseDTO obtener(Long id);

    MonedaResponseDTO crear(MonedaCreateDTO dto);

    MonedaResponseDTO actualizar(Long id, MonedaUpdateDTO dto);

    void eliminar(Long id);
}
