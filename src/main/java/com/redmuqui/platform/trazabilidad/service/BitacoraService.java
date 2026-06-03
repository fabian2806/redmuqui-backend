package com.redmuqui.platform.trazabilidad.service;

import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.trazabilidad.dto.BitacoraResponseDTO;
import com.redmuqui.platform.trazabilidad.entity.Bitacora;
import com.redmuqui.platform.trazabilidad.repository.BitacoraRepository;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de bitácora. Provee:
 *   - listado y filtrado de eventos (RF-064 a RF-066).
 *   - método de registro programático para que otros services anoten eventos automáticamente
 *     (RF-062, RF-063).
 */
@Service
@RequiredArgsConstructor
public class BitacoraService {

    private final BitacoraRepository bitacoraRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<BitacoraResponseDTO> listar(Pageable pageable) {
        return bitacoraRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<BitacoraResponseDTO> listarPorEntidad(String entidad, Long idEntidad, Pageable pageable) {
        return bitacoraRepository.findByEntidadReferenciadaAndIdEntidadRef(entidad, idEntidad, pageable)
            .map(this::toDTO);
    }

    /**
     * Registra un evento en la bitácora. Pensado para ser llamado desde otros services
     * tras ejecutar acciones relevantes.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEvento(String tipoAccion, String entidad, Long idEntidad,
                                 String descripcion, Long idUsuario) {
        Bitacora bitacora = Bitacora.builder()
            .tipoAccion(tipoAccion)
            .entidadReferenciada(entidad)
            .idEntidadRef(idEntidad)
            .descripcion(descripcion)
            .fecha(LocalDateTime.now())
            .usuario(usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", idUsuario)))
            .build();
        bitacoraRepository.save(bitacora);
    }

    /**
     * Registra un evento usando el usuario autenticado del {@link SecurityContextHolder}
     * (poblado por {@link com.redmuqui.platform.auth.filter.JwtAuthenticationFilter}).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEventoAutenticado(String tipoAccion, String entidad, Long idEntidad,
                                           String descripcion) {
        registrarEvento(tipoAccion, entidad, idEntidad, descripcion, resolverIdUsuarioAutenticado());
    }

    Long resolverIdUsuarioAutenticado() {
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

    private BitacoraResponseDTO toDTO(Bitacora b) {
        return new BitacoraResponseDTO(
            b.getId(), b.getTipoAccion(), b.getFecha(), b.getDescripcion(),
            b.getEntidadReferenciada(), b.getIdEntidadRef(),
            b.getUsuario() != null ? b.getUsuario().getId() : null,
            b.getUsuario() != null ? b.getUsuario().getNombres() + " " + b.getUsuario().getApellidos() : null
        );
    }
}
