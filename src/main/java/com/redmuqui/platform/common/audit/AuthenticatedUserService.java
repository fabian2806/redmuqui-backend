package com.redmuqui.platform.common.audit;

import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.usuario.entity.Usuario;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserService {

    private final UsuarioRepository usuarioRepository;

    public Usuario obtenerUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto de seguridad");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            throw new IllegalStateException("El principal autenticado no es válido");
        }
        return usuarioRepository.findByEmailIgnoreCase(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", userDetails.getUsername()));
    }

    public Long obtenerIdUsuario() {
        return obtenerUsuario().getId();
    }
}
