package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.dto.HitoCreateDTO;
import com.redmuqui.platform.actividad.dto.HitoResponseDTO;
import com.redmuqui.platform.actividad.entity.EstadoHito;
import com.redmuqui.platform.actividad.entity.Hito;
import com.redmuqui.platform.actividad.repository.HitoRepository;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.proyecto.entity.Proyecto;
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
    private final ActividadRepository actividadRepository;
    private final AvanceProyectoService avanceProyectoService;

    @Transactional(readOnly = true)
    public List<HitoResponseDTO> listarPorProyecto(Long idProyecto) {
        validarProyectoExiste(idProyecto);
        return hitoRepository.findByProyectoIdOrderByFechaClaveAscIdAsc(idProyecto).stream().map(this::toDTO).toList();
    }

    @Transactional
    public HitoResponseDTO crear(HitoCreateDTO dto) {
        if (dto.idProyecto() == null) {
            throw new BusinessException("El proyecto es obligatorio para registrar un hito");
        }
        return crear(dto.idProyecto(), dto);
    }

    @Transactional
    public HitoResponseDTO crear(Long idProyecto, HitoCreateDTO dto) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
            .orElseThrow(() -> new ResourceNotFoundException("Proyecto", idProyecto));

        Hito hito = Hito.builder()
            .nombre(dto.nombre())
            .descripcion(dto.descripcion())
            .fechaClave(dto.fechaClave())
            .estado(EstadoHito.PENDIENTE)
            .proyecto(proyecto)
            .build();
        return toDTO(hitoRepository.save(hito));
    }

    @Transactional
    public HitoResponseDTO actualizar(Long idProyecto, Long id, HitoCreateDTO dto) {
        Hito hito = hitoRepository.findByIdAndProyectoId(id, idProyecto)
            .orElseThrow(() -> new ResourceNotFoundException("Hito", id));

        hito.setNombre(dto.nombre());
        hito.setDescripcion(dto.descripcion());
        hito.setFechaClave(dto.fechaClave());

        return toDTO(hito);
    }

    @Transactional
    public void eliminar(Long idProyecto, Long id) {
        Hito hito = hitoRepository.findByIdAndProyectoId(id, idProyecto)
            .orElseThrow(() -> new ResourceNotFoundException("Hito", id));
        if (actividadRepository.existsByHitoId(id)) {
            throw new BusinessException("No se puede eliminar el hito porque tiene actividades relacionadas");
        }
        hitoRepository.delete(hito);
        hitoRepository.flush();
        avanceProyectoService.recalcularProyecto(idProyecto);
    }

    @Transactional
    public HitoResponseDTO cambiarEstado(Long id, EstadoHito nuevoEstado) {
        Hito h = hitoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hito", id));
        h.setEstado(nuevoEstado);
        return toDTO(h);
    }

    private HitoResponseDTO toDTO(Hito h) {
        AvanceProyectoService.ResumenHito resumen = avanceProyectoService.resumir(h);
        return new HitoResponseDTO(
            h.getId(), h.getNombre(), h.getDescripcion(),
            h.getFechaClave(), resumen.estado(),
            h.getProyecto().getId(),
            resumen.porcentajeAvance(), resumen.fechaInicio(), resumen.fechaFin(), resumen.duracionDias(),
            resumen.totalActividades(), resumen.actividadesFinalizadas(),
            h.getFechaCreacion(), h.getFechaModificacion()
        );
    }

    private void validarProyectoExiste(Long idProyecto) {
        if (!proyectoRepository.existsById(idProyecto)) {
            throw new ResourceNotFoundException("Proyecto", idProyecto);
        }
    }
}
