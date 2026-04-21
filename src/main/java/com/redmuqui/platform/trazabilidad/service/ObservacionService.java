package com.redmuqui.platform.trazabilidad.service;

import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.trazabilidad.dto.ObservacionCreateDTO;
import com.redmuqui.platform.trazabilidad.dto.ObservacionResponseDTO;
import com.redmuqui.platform.trazabilidad.entity.EstadoObservacion;
import com.redmuqui.platform.trazabilidad.entity.Observacion;
import com.redmuqui.platform.trazabilidad.repository.ObservacionRepository;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ObservacionService {

    private final ObservacionRepository observacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ObservacionResponseDTO> listarPorEntidad(String entidad, Long idEntidad) {
        return observacionRepository.findByEntidadReferenciadaAndIdEntidadRef(entidad, idEntidad)
            .stream().map(this::toDTO).toList();
    }

    @Transactional
    public ObservacionResponseDTO crear(ObservacionCreateDTO dto, Long idUsuario) {
        Observacion observacion = Observacion.builder()
            .descripcion(dto.descripcion())
            .entidadReferenciada(dto.entidadReferenciada())
            .idEntidadRef(dto.idEntidadRef())
            .fecha(LocalDateTime.now())
            .estado(EstadoObservacion.PENDIENTE)
            .usuario(usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", idUsuario)))
            .build();
        return toDTO(observacionRepository.save(observacion));
    }

    @Transactional
    public ObservacionResponseDTO cambiarEstado(Long id, EstadoObservacion nuevoEstado) {
        Observacion o = observacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Observacion", id));
        o.setEstado(nuevoEstado);
        return toDTO(o);
    }

    private ObservacionResponseDTO toDTO(Observacion o) {
        return new ObservacionResponseDTO(
            o.getId(), o.getDescripcion(), o.getFecha(), o.getEstado(),
            o.getEntidadReferenciada(), o.getIdEntidadRef(),
            o.getUsuario() != null ? o.getUsuario().getId() : null,
            o.getUsuario() != null ? o.getUsuario().getNombres() + " " + o.getUsuario().getApellidos() : null
        );
    }
}
