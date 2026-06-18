package com.redmuqui.platform.usuario.service;

import com.redmuqui.platform.auth.dto.TokenResponse;
import com.redmuqui.platform.auth.service.JwtService;
import com.redmuqui.platform.common.exception.DuplicateResourceException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.institucion.repository.InstitucionRepository;
import com.redmuqui.platform.rol.repository.RolRepository;
import com.redmuqui.platform.usuario.dto.UsuarioCreateDTO;
import com.redmuqui.platform.usuario.dto.UsuarioPerfilUpdateDTO;
import com.redmuqui.platform.usuario.dto.UsuarioPerfilUpdateResponseDTO;
import com.redmuqui.platform.usuario.dto.UsuarioResponseDTO;
import com.redmuqui.platform.usuario.dto.UsuarioUpdateDTO;
import com.redmuqui.platform.usuario.entity.Usuario;
import com.redmuqui.platform.usuario.mapper.UsuarioMapper;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final InstitucionRepository institucionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper mapper;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(mapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        return mapper.toResponseDTO(buscarOFallar(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", email));
        return mapper.toResponseDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO crear(UsuarioCreateDTO dto) {
        if (usuarioRepository.existsByEmailIgnoreCase(dto.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con el correo: " + dto.email());
        }

        Usuario usuario = Usuario.builder()
            .nombres(dto.nombres())
            .apellidos(dto.apellidos())
            .email(dto.email())
            .telefono(dto.telefono())
            .contrasenhaHash(passwordEncoder.encode(dto.contrasenha()))
            .estado(true)
            .rol(rolRepository.findById(dto.idRol())
                .orElseThrow(() -> new ResourceNotFoundException("Rol", dto.idRol())))
            .build();

        if (dto.idInstitucion() != null) {
            usuario.setInstitucion(institucionRepository.findById(dto.idInstitucion())
                .orElseThrow(() -> new ResourceNotFoundException("Institucion", dto.idInstitucion())));
        }

        usuario.setIntentosLoginFallidos(0);
        usuario.setBloqueadoHasta(null);

        return mapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = buscarOFallar(id);

        // Si cambia el email, validar que no exista en otro usuario
        if (!usuario.getEmail().equalsIgnoreCase(dto.email())
            && usuarioRepository.existsByEmailIgnoreCase(dto.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con el correo: " + dto.email());
        }

        usuario.setNombres(dto.nombres());
        usuario.setApellidos(dto.apellidos());
        usuario.setEmail(dto.email());
        usuario.setTelefono(dto.telefono());
        usuario.setRol(rolRepository.findById(dto.idRol())
            .orElseThrow(() -> new ResourceNotFoundException("Rol", dto.idRol())));

        usuario.setInstitucion(dto.idInstitucion() != null
            ? institucionRepository.findById(dto.idInstitucion())
                .orElseThrow(() -> new ResourceNotFoundException("Institucion", dto.idInstitucion()))
            : null);

        return mapper.toResponseDTO(usuario);
    }

    @Transactional
    public UsuarioPerfilUpdateResponseDTO actualizarPerfil(String emailActual, UsuarioPerfilUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailActual)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", emailActual));

        if (!usuario.getEmail().equalsIgnoreCase(dto.email())
            && usuarioRepository.existsByEmailIgnoreCase(dto.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con el correo: " + dto.email());
        }

        usuario.setNombres(dto.nombres());
        usuario.setApellidos(dto.apellidos());
        usuario.setEmail(dto.email());
        usuario.setTelefono(dto.telefono());
        usuarioRepository.flush();

        UsuarioResponseDTO usuarioActualizado = mapper.toResponseDTO(usuario);
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        TokenResponse tokens = TokenResponse.of(
            jwtService.generateAccessToken(userDetails),
            jwtService.generateRefreshToken(userDetails),
            jwtService.getAccessTokenExpirationMs()
        );

        return new UsuarioPerfilUpdateResponseDTO(usuarioActualizado, tokens);
    }

    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Usuario usuario = buscarOFallar(id);
        usuario.setEstado(activo);
    }

    private Usuario buscarOFallar(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}
