package com.redmuqui.platform.trazabilidad.service;

import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.audit.AuthenticatedUserService;
import com.redmuqui.platform.trazabilidad.dto.ObservacionRequestDTO;
import com.redmuqui.platform.trazabilidad.dto.ObservacionResponseDTO;
import com.redmuqui.platform.trazabilidad.dto.ResolverIncidenciaRequestDTO;
import com.redmuqui.platform.trazabilidad.entity.EstadoObservacion;
import com.redmuqui.platform.trazabilidad.entity.Observacion;
import com.redmuqui.platform.trazabilidad.entity.CriticidadIncidencia;
import com.redmuqui.platform.trazabilidad.repository.ObservacionRepository;
import com.redmuqui.platform.usuario.entity.Usuario;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ObservacionService {

    private final ObservacionRepository observacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public ObservacionResponseDTO crear(ObservacionRequestDTO dto) {
        Long idUsuario = resolverIdUsuarioAutenticado();
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", idUsuario));

        CriticidadIncidencia criticidad = dto.criticidad() != null
            ? dto.criticidad()
            : CriticidadIncidencia.MEDIA;
        Usuario responsable = dto.idResponsable() != null
            ? usuarioRepository.findById(dto.idResponsable())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idResponsable()))
            : usuario;

        Observacion observacion = Observacion.builder()
            .descripcion(dto.descripcion())
            .entidadReferenciada(dto.entidadReferenciada())
            .idEntidadRef(dto.idEntidadReferenciada())
            .fecha(LocalDateTime.now())
            .estado(EstadoObservacion.PENDIENTE)
            .criticidad(criticidad)
            .fechaVencimiento(LocalDateTime.now().plusDays(criticidad.getDiasResolucion()))
            .usuario(usuario)
            .responsable(responsable)
            .build();

        return toDTO(observacionRepository.save(observacion));
    }

    @Transactional
    public ObservacionResponseDTO cambiarEstado(Long id, EstadoObservacion estado) {
        Observacion observacion = observacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incidencia", id));
        if (estado == EstadoObservacion.RESUELTA) {
            throw new BusinessException("Para resolver una incidencia debe registrar el comentario de resolución");
        }
        observacion.setEstado(estado);
        observacion.setFechaResolucion(null);
        observacion.setComentarioResolucion(null);
        observacion.setUsuarioResolucion(null);
        return toDTO(observacion);
    }

    @Transactional
    public ObservacionResponseDTO resolver(Long id, ResolverIncidenciaRequestDTO request) {
        Observacion observacion = observacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incidencia", id));
        if (observacion.getEstado() == EstadoObservacion.RESUELTA) {
            throw new BusinessException("La incidencia ya se encuentra resuelta");
        }
        observacion.setEstado(EstadoObservacion.RESUELTA);
        observacion.setFechaResolucion(LocalDateTime.now());
        observacion.setComentarioResolucion(request.comentario().trim());
        observacion.setUsuarioResolucion(authenticatedUserService.obtenerUsuario());
        return toDTO(observacion);
    }

    @Transactional(readOnly = true)
    public Page<ObservacionResponseDTO> listarPorEntidad(
        String entidadReferenciada, Long idEntidadReferenciada, Pageable pageable
    ) {
        return observacionRepository
            .findByEntidadReferenciadaAndIdEntidadRefOrderByFechaDesc(
                entidadReferenciada, idEntidadReferenciada, pageable)
            .map(this::toDTO);
    }

    private Long resolverIdUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto de seguridad");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            throw new IllegalStateException("El principal de autenticación no es un UserDetails válido");
        }

        return usuarioRepository.findByEmailIgnoreCase(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", userDetails.getUsername()))
            .getId();
    }

    private ObservacionResponseDTO toDTO(Observacion o) {
        Usuario usuario = o.getUsuario();
        String nombreUsuario = null;
        String emailUsuario = null;
        Long idUsuario = null;
        Long idResponsable = null;
        String nombreResponsable = null;
        Long idUsuarioResolucion = null;
        String nombreUsuarioResolucion = null;

        if (usuario != null) {
            idUsuario = usuario.getId();
            emailUsuario = usuario.getEmail();
            nombreUsuario = (usuario.getNombres() + " " + usuario.getApellidos()).trim();
        }
        if (o.getResponsable() != null) {
            idResponsable = o.getResponsable().getId();
            nombreResponsable = (o.getResponsable().getNombres() + " "
                + o.getResponsable().getApellidos()).trim();
        }
        if (o.getUsuarioResolucion() != null) {
            idUsuarioResolucion = o.getUsuarioResolucion().getId();
            nombreUsuarioResolucion = (o.getUsuarioResolucion().getNombres() + " "
                + o.getUsuarioResolucion().getApellidos()).trim();
        }

        return new ObservacionResponseDTO(
            o.getId(),
            o.getDescripcion(),
            o.getFecha(),
            o.getEstado(),
            o.getCriticidad(),
            o.getFechaVencimiento(),
            o.getFechaResolucion(),
            o.getComentarioResolucion(),
            idUsuarioResolucion,
            nombreUsuarioResolucion,
            o.getEntidadReferenciada(),
            o.getIdEntidadRef(),
            idUsuario,
            nombreUsuario,
            emailUsuario,
            idResponsable,
            nombreResponsable
        );
    }
}
