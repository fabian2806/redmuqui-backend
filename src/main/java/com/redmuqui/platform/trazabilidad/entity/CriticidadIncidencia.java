package com.redmuqui.platform.trazabilidad.entity;

public enum CriticidadIncidencia {
    BAJA(14),
    MEDIA(7),
    ALTA(3),
    CRITICA(1);

    private final int diasResolucion;

    CriticidadIncidencia(int diasResolucion) {
        this.diasResolucion = diasResolucion;
    }

    public int getDiasResolucion() {
        return diasResolucion;
    }
}
