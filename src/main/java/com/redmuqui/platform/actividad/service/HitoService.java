package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.dto.HitoCreateDTO;
import com.redmuqui.platform.actividad.dto.HitoResponseDTO;
import com.redmuqui.platform.actividad.entity.EstadoHito;
import com.redmuqui.platform.actividad.entity.Hito;
import com.redmuqui.platform.actividad.repository.HitoRepository;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HitoService {

    private final HitoRepository hitoRepository;
    private final ProyectoRepository proyectoRepository;

    @Transactional(readOnly = true)
    public List<HitoResponseDTO> listarPorProyecto(Long idProyecto) {
        return hitoRepository.findByProyectoId(idProyecto).stream().map(this::toDTO).toList();
    }

    @Transactional
    public HitoResponseDTO crear(HitoCreateDTO dto) {
        Hito hito = Hito.builder()
            .nombre(dto.nombre())
            .descripcion(dto.descripcion())
            .fechaClave(dto.fechaClave())
            .estado(dto.estado() != null ? dto.estado() : EstadoHito.PENDIENTE)
            .proyecto(proyectoRepository.findById(dto.idProyecto())
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto", dto.idProyecto())))
            .build();
        return toDTO(hitoRepository.save(hito));
    }

    @Transactional
    public HitoResponseDTO cambiarEstado(Long id, EstadoHito nuevoEstado) {
        Hito h = hitoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hito", id));
        h.setEstado(nuevoEstado);
        return toDTO(h);
    }

    private HitoResponseDTO toDTO(Hito h) {
        return new HitoResponseDTO(
            h.getId(), h.getNombre(), h.getDescripcion(),
            h.getFechaClave(), h.getEstado(),
            h.getProyecto().getId()
        );
    }
}
