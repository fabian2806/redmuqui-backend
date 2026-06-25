package com.redmuqui.platform.config.service;

import com.redmuqui.platform.config.dto.ConfiguracionSistemaDTO;
import com.redmuqui.platform.config.entity.ConfiguracionSistema;
import com.redmuqui.platform.config.repository.ConfiguracionSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConfiguracionSistemaService {

    private static final Long CONFIG_ID = 1L;

    private final ConfiguracionSistemaRepository repository;

    public ConfiguracionSistemaDTO obtenerConfiguracion() {
        ConfiguracionSistema config = obtenerEntidadConfiguracion();
        return convertirADTO(config);
    }

    public ConfiguracionSistemaDTO actualizarConfiguracion(ConfiguracionSistemaDTO dto) {
        ConfiguracionSistema config = obtenerEntidadConfiguracion();

        config.setNombreOrganizacion(dto.general().nombreOrganizacion());
        config.setNombrePlataforma(dto.general().nombrePlataforma());
        config.setCorreoSoporte(dto.general().correoSoporte());
        config.setTelefono(dto.general().telefono());
        config.setDireccion(dto.general().direccion());
        config.setSistemaActivo(dto.general().sistemaActivo());

        config.setTamanioMaximoMb(dto.documentos().tamanioMaximoMb());
        config.setCantidadMaximaAdjuntos(dto.documentos().cantidadMaximaAdjuntos());
        config.setEstadoInicial(dto.documentos().estadoInicial());

        config.setIntentosMaximosLogin(dto.seguridad().intentosMaximosLogin());
        config.setTiempoBloqueoMinutos(dto.seguridad().tiempoBloqueoMinutos());
        config.setDuracionAccessTokenMinutos(dto.seguridad().duracionAccessTokenMinutos());
        config.setDuracionRefreshTokenDias(dto.seguridad().duracionRefreshTokenDias());
        config.setRecuperacionPassword(dto.seguridad().recuperacionPassword());
        config.setCierrePorInactividad(dto.seguridad().cierrePorInactividad());

        config.setActualizadoEn(LocalDateTime.now());

        ConfiguracionSistema actualizado = repository.save(config);

        return convertirADTO(actualizado);
    }

    public ConfiguracionSistema obtenerEntidadConfiguracion() {
        return repository.findById(CONFIG_ID)
                .orElseThrow(() -> new RuntimeException("No existe configuración del sistema"));
    }

    private ConfiguracionSistemaDTO convertirADTO(ConfiguracionSistema config) {
        return new ConfiguracionSistemaDTO(
                new ConfiguracionSistemaDTO.General(
                        config.getNombreOrganizacion(),
                        config.getNombrePlataforma(),
                        config.getCorreoSoporte(),
                        config.getTelefono(),
                        config.getDireccion(),
                        config.getSistemaActivo()
                ),
                new ConfiguracionSistemaDTO.Documentos(
                        config.getTamanioMaximoMb(),
                        config.getCantidadMaximaAdjuntos(),
                        config.getEstadoInicial()
                ),
                new ConfiguracionSistemaDTO.Seguridad(
                        config.getIntentosMaximosLogin(),
                        config.getTiempoBloqueoMinutos(),
                        config.getDuracionAccessTokenMinutos(),
                        config.getDuracionRefreshTokenDias(),
                        config.getRecuperacionPassword(),
                        config.getCierrePorInactividad()
                )
        );
    }
}