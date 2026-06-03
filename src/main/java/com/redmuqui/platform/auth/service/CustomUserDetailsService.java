package com.redmuqui.platform.auth.service;

import com.redmuqui.platform.usuario.entity.Usuario;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Implementación de UserDetailsService que carga usuarios desde la BD.
 * Construye los GrantedAuthority a partir del rol del usuario y los permisos asociados.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    private static final Map<String, Set<String>> FALLBACK_PERMISSIONS_BY_ROLE = Map.of(
        "ADMINISTRADOR", Set.of(
            "USUARIOS_READ", "USUARIOS_CREATE", "USUARIOS_UPDATE", "USUARIOS_DEACTIVATE",
            "CATALOGOS_READ", "CATALOGOS_MANAGE",
            "PROYECTOS_READ", "PROYECTOS_CREATE", "PROYECTOS_UPDATE",
            "DOCUMENTOS_READ", "DOCUMENTOS_CREATE", "DOCUMENTOS_UPDATE", "DOCUMENTOS_VALIDATE",
            "BITACORA_READ",
            "REPORTES_READ", "REPORTES_EXPORT"
        ),
        "TECNICO", Set.of(
            "CATALOGOS_READ",
            "PROYECTOS_READ", "PROYECTOS_CREATE", "PROYECTOS_UPDATE",
            "DOCUMENTOS_READ", "DOCUMENTOS_CREATE", "DOCUMENTOS_UPDATE",
            "BITACORA_READ",
            "REPORTES_READ"
        ),
        "COORDINADOR", Set.of(
            "USUARIOS_READ",
            "CATALOGOS_READ",
            "PROYECTOS_READ", "PROYECTOS_CREATE", "PROYECTOS_UPDATE",
            "DOCUMENTOS_READ", "DOCUMENTOS_CREATE", "DOCUMENTOS_UPDATE", "DOCUMENTOS_VALIDATE",
            "BITACORA_READ",
            "REPORTES_READ", "REPORTES_EXPORT"
        ),
        "CONSULTOR", Set.of(
            "CATALOGOS_READ",
            "PROYECTOS_READ",
            "DOCUMENTOS_READ",
            "REPORTES_READ"
        )
    );

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        if (Boolean.FALSE.equals(usuario.getEstado())) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        List<GrantedAuthority> authorities = buildAuthorities(usuario);

        return new User(
            usuario.getEmail(),
            usuario.getContrasenhaHash(),
            usuario.getEstado(),
            true,
            true,
            true,
            authorities
        );
    }

    private List<GrantedAuthority> buildAuthorities(Usuario usuario) {
        String roleName = usuario.getRol().getNombre().toUpperCase();

        // ROLE_<NOMBRE> + permisos individuales como authorities
        Stream<GrantedAuthority> rolStream = Stream.of(
            new SimpleGrantedAuthority("ROLE_" + roleName)
        );

        Stream<GrantedAuthority> permisosStream = usuario.getRol().getPermisos().stream()
            .map(p -> new SimpleGrantedAuthority(p.getNombre()));

        Stream<GrantedAuthority> fallbackPermisosStream = FALLBACK_PERMISSIONS_BY_ROLE
            .getOrDefault(roleName, Set.of())
            .stream()
            .map(SimpleGrantedAuthority::new);

        return Stream.concat(rolStream, Stream.concat(permisosStream, fallbackPermisosStream))
            .distinct()
            .toList();
    }
}
