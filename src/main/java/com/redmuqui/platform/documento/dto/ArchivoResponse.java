package com.redmuqui.platform.documento.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ArchivoResponse {

    private Long id;
    private String nombre;
    private String url;
    private String extension;
    private String tipoContenido;
    private String descripcion;
    private Long tamanioBytes;
}