package com.redmuqui.platform.trazabilidad.service;

import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.trazabilidad.dto.ObservacionRequestDTO;
import com.redmuqui.platform.trazabilidad.dto.ObservacionResponseDTO;
import com.redmuqui.platform.trazabilidad.entity.EstadoObservacion;
import com.redmuqui.platform.trazabilidad.entity.Observacion;
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

    @Transactional
    public ObservacionResponseDTO crear(ObservacionRequestDTO dto) {
        Long idUsuario = resolverIdUsuarioAutenticado();
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", idUsuario));

        Observacion observacion = Observacion.builder()
            .descripcion(dto.descripcion())
            .entidadReferenciada(dto.entidadReferenciada())
            .idEntidadRef(dto.idEntidadReferenciada())
            .fecha(LocalDateTime.now())
            .estado(EstadoObservacion.PENDIENTE)
            .usuario(usuario)
            .build();

        return toDTO(observacionRepository.save(observacion));
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

        if (usuario != null) {
            idUsuario = usuario.getId();
            emailUsuario = usuario.getEmail();
            nombreUsuario = (usuario.getNombres() + " " + usuario.getApellidos()).trim();
        }

        return new ObservacionResponseDTO(
            o.getId(),
            o.getDescripcion(),
            o.getFecha(),
            o.getEstado(),
            o.getEntidadReferenciada(),
            o.getIdEntidadRef(),
            idUsuario,
            nombreUsuario,
            emailUsuario
        );
    }
}
