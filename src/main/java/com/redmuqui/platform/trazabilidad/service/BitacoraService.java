package com.redmuqui.platform.trazabilidad.service;

import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.trazabilidad.dto.BitacoraConsultaDTO;
import com.redmuqui.platform.trazabilidad.entity.Bitacora;
import com.redmuqui.platform.trazabilidad.repository.BitacoraRepository;
import com.redmuqui.platform.usuario.entity.Usuario;
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
    public Page<BitacoraConsultaDTO> consultarGeneral(String q, Pageable pageable) {
        return (q == null || q.isBlank()
            ? bitacoraRepository.findAllByOrderByFechaDesc(pageable)
            : bitacoraRepository.buscar(q.trim(), pageable))
            .map(this::toConsultaDTO);
    }

    public Page<BitacoraConsultaDTO> consultarGeneral(Pageable pageable) {
        return consultarGeneral(null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BitacoraConsultaDTO> consultarHistorialEntidad(
        String entidadReferenciada, Long idEntidadRef, String q, Pageable pageable
    ) {
        return (q == null || q.isBlank()
            ? bitacoraRepository.findByEntidadReferenciadaAndIdEntidadRefOrderByFechaDesc(
                entidadReferenciada, idEntidadRef, pageable)
            : bitacoraRepository.buscarEntidad(
                entidadReferenciada, idEntidadRef, q.trim(), pageable))
            .map(this::toConsultaDTO);
    }

    public Page<BitacoraConsultaDTO> consultarHistorialEntidad(
        String entidadReferenciada, Long idEntidadRef, Pageable pageable
    ) {
        return consultarHistorialEntidad(entidadReferenciada, idEntidadRef, null, pageable);
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

    private BitacoraConsultaDTO toConsultaDTO(Bitacora bitacora) {
        return new BitacoraConsultaDTO(
            resolverNombreUsuario(bitacora.getUsuario()),
            bitacora.getDescripcion(),
            bitacora.getTipoAccion(),
            bitacora.getFecha()
        );
    }

    private String resolverNombreUsuario(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        String nombres = usuario.getNombres() != null ? usuario.getNombres().trim() : " ";
        String apellidos = usuario.getApellidos() != null ? usuario.getApellidos().trim() : " ";

        String nombreCompleto = (nombres + " " + apellidos).trim();
        return nombreCompleto.isBlank() ? usuario.getEmail() : nombreCompleto;
    }
}
