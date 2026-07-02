package com.redmuqui.platform.tipodocumento.service;

import com.redmuqui.platform.tipodocumento.dto.TipoDocumentoCreateDTO;
import com.redmuqui.platform.tipodocumento.dto.TipoDocumentoResponseDTO;
import com.redmuqui.platform.tipodocumento.dto.TipoDocumentoUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TipoDocumentoService {

    List<TipoDocumentoResponseDTO> listar();

    Page<TipoDocumentoResponseDTO> listarPaginado(Pageable pageable);

    TipoDocumentoResponseDTO obtener(Long id);

    TipoDocumentoResponseDTO crear(TipoDocumentoCreateDTO dto);

    TipoDocumentoResponseDTO actualizar(Long id, TipoDocumentoUpdateDTO dto);

    void eliminar(Long id);
}
