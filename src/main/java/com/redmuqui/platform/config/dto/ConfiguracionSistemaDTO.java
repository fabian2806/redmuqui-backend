package com.redmuqui.platform.config.dto;

public record ConfiguracionSistemaDTO(
        General general,
        Documentos documentos,
        Seguridad seguridad
) {
    public record General(
            String nombreOrganizacion,
            String nombrePlataforma,
            String correoSoporte,
            String telefono,
            String direccion,
            Boolean sistemaActivo
    ) {}

    public record Documentos(
            Integer tamanioMaximoMb,
            Integer cantidadMaximaAdjuntos,
            String estadoInicial
    ) {}

    public record Seguridad(
            Integer intentosMaximosLogin,
            Integer tiempoBloqueoMinutos,
            Integer duracionAccessTokenMinutos,
            Integer duracionRefreshTokenDias,
            Boolean recuperacionPassword,
            Boolean cierrePorInactividad
    ) {}
}