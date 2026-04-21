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
import java.util.stream.Stream;

/**
 * Implementación de UserDetailsService que carga usuarios desde la BD.
 * Construye los GrantedAuthority a partir del rol del usuario y los permisos asociados.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

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
        // ROLE_<NOMBRE> + permisos individuales como authorities
        Stream<GrantedAuthority> rolStream = Stream.of(
            new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre().toUpperCase())
        );

        Stream<GrantedAuthority> permisosStream = usuario.getRol().getPermisos().stream()
            .map(p -> new SimpleGrantedAuthority(p.getNombre()));

        return Stream.concat(rolStream, permisosStream).toList();
    }
}
