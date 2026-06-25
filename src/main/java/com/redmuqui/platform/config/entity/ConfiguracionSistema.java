package com.redmuqui.platform.config.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "configuracion_sistema")
public class ConfiguracionSistema {

    @Id
    private Long id;

    @Column(name = "nombre_organizacion", nullable = false)
    private String nombreOrganizacion;

    @Column(name = "nombre_plataforma", nullable = false)
    private String nombrePlataforma;

    @Column(name = "correo_soporte")
    private String correoSoporte;

    private String telefono;

    private String direccion;

    @Column(name = "sistema_activo", nullable = false)
    private Boolean sistemaActivo;

    @Column(name = "tamanio_maximo_mb", nullable = false)
    private Integer tamanioMaximoMb;

    @Column(name = "cantidad_maxima_adjuntos", nullable = false)
    private Integer cantidadMaximaAdjuntos;

    @Column(name = "estado_inicial", nullable = false)
    private String estadoInicial;

    @Column(name = "intentos_maximos_login", nullable = false)
    private Integer intentosMaximosLogin;

    @Column(name = "tiempo_bloqueo_minutos", nullable = false)
    private Integer tiempoBloqueoMinutos;

    @Column(name = "duracion_access_token_minutos", nullable = false)
    private Integer duracionAccessTokenMinutos;

    @Column(name = "duracion_refresh_token_dias", nullable = false)
    private Integer duracionRefreshTokenDias;

    @Column(name = "recuperacion_password", nullable = false)
    private Boolean recuperacionPassword;

    @Column(name = "cierre_por_inactividad", nullable = false)
    private Boolean cierrePorInactividad;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}