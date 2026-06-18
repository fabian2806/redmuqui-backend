package com.redmuqui.platform.proyecto.dto;

import java.util.List;

public record OrganigramaProyectoDTO(
    PersonaNodo responsableProyecto,
    List<FaseNodo> fases
) {
    public record PersonaNodo(Long idUsuario, String nombre, String rol) {}
    public record FaseNodo(
        Long idFase,
        String nombre,
        List<ActividadNodo> actividades
    ) {}
    public record ActividadNodo(
        Long idActividad,
        String nombre,
        List<PersonaNodo> responsables,
        List<SubactividadNodo> subactividades
    ) {}
    public record SubactividadNodo(
        Long idSubactividad,
        String nombre,
        PersonaNodo responsable
    ) {}
}
