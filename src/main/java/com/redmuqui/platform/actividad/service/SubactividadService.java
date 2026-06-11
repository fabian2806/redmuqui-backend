package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.dto.*;
import com.redmuqui.platform.actividad.entity.*;
import com.redmuqui.platform.actividad.repository.*;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubactividadService {

    private final SubactividadRepository subactividadRepository;
    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final SubactividadCofinanciamientoRepository cofinanciamientoRepository;
    private final SubactividadArchivoRepository archivoRepository;
    private final AvanceProyectoService avanceProyectoService;
    
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    @Transactional
    public SubactividadResponseDTO crear(Long actividadId, SubactividadCreateDTO dto) {
        Actividad actividad = actividadRepository.findById(actividadId)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad", actividadId));
            
        Subactividad subactividad = Subactividad.builder()
            .nombre(dto.nombre())
            .responsable(usuarioRepository.findById(dto.idResponsable())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idResponsable())))
            .presupuesto(dto.presupuesto() != null ? dto.presupuesto() : 0.0)
            .hombresInvolucrados(dto.hombresInvolucrados() != null ? dto.hombresInvolucrados() : 0)
            .mujeresInvolucradas(dto.mujeresInvolucradas() != null ? dto.mujeresInvolucradas() : 0)
            .fechaInicio(dto.fechaInicio())
            .fechaFin(dto.fechaFin())
            .estado(dto.estado() != null ? dto.estado() : EstadoSubactividad.PENDIENTE)
            .descripcion(dto.descripcion())
            .actividad(actividad)
            .build();

        Subactividad guardada = subactividadRepository.save(subactividad);
        avanceProyectoService.recalcularActividad(actividad.getId());
        return toDTO(guardada);
    }

    @Transactional
    public SubactividadResponseDTO actualizar(Long subactividadId, SubactividadCreateDTO dto) {
        Subactividad s = subactividadRepository.findById(subactividadId)
            .orElseThrow(() -> new ResourceNotFoundException("Subactividad", subactividadId));

        s.setNombre(dto.nombre());
        s.setDescripcion(dto.descripcion());
        if (dto.idResponsable() != null) {
            s.setResponsable(usuarioRepository.findById(dto.idResponsable())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.idResponsable())));
        }
        s.setPresupuesto(dto.presupuesto() != null ? dto.presupuesto() : 0.0);
        s.setHombresInvolucrados(dto.hombresInvolucrados() != null ? dto.hombresInvolucrados() : 0);
        s.setMujeresInvolucradas(dto.mujeresInvolucradas() != null ? dto.mujeresInvolucradas() : 0);
        s.setFechaInicio(dto.fechaInicio());
        s.setFechaFin(dto.fechaFin());
        s.setEstado(dto.estado() != null ? dto.estado() : EstadoSubactividad.PENDIENTE);

        Subactividad guardada = subactividadRepository.save(s);
        avanceProyectoService.recalcularActividad(guardada.getActividad().getId());
        return toDTO(guardada);
    }

    @Transactional
    public void eliminar(Long subactividadId) {
        Subactividad s = subactividadRepository.findById(subactividadId)
            .orElseThrow(() -> new ResourceNotFoundException("Subactividad", subactividadId));
        Long actividadId = s.getActividad().getId();
        subactividadRepository.delete(s);
        subactividadRepository.flush();
        avanceProyectoService.recalcularActividad(actividadId);
    }

    @Transactional
    public SubactividadResponseDTO cofinanciar(Long subactividadId, SubactividadCofinanciamientoCreateDTO dto) {
        Subactividad subactividad = subactividadRepository.findById(subactividadId)
            .orElseThrow(() -> new ResourceNotFoundException("Subactividad", subactividadId));
            
        Actividad actividadOrigen = actividadRepository.findById(dto.actividadId())
            .orElseThrow(() -> new ResourceNotFoundException("Actividad", dto.actividadId()));
            
        SubactividadCofinanciamiento cofinanciamiento = SubactividadCofinanciamiento.builder()
            .subactividad(subactividad)
            .actividadOrigen(actividadOrigen)
            .monto(dto.monto())
            .build();
            
        // Check if cofinanciamiento already exists for this pair
        SubactividadCofinanciamientoId id = new SubactividadCofinanciamientoId(subactividadId, dto.actividadId());
        if (cofinanciamientoRepository.existsById(id)) {
            SubactividadCofinanciamiento existing = cofinanciamientoRepository.findById(id).get();
            existing.setMonto(existing.getMonto() + dto.monto());
            cofinanciamientoRepository.save(existing);
        } else {
            cofinanciamiento.setId(id);
            subactividad.addCofinanciamiento(cofinanciamiento);
            cofinanciamientoRepository.save(cofinanciamiento);
        }
        
        return toDTO(subactividadRepository.save(subactividad));
    }
    
    @Transactional
    public SubactividadResponseDTO subirEvidencia(Long subactividadId, MultipartFile file, Integer hombres, Integer mujeres) {
        Subactividad subactividad = subactividadRepository.findById(subactividadId)
            .orElseThrow(() -> new ResourceNotFoundException("Subactividad", subactividadId));
            
        if (hombres != null) subactividad.setHombresInvolucrados(subactividad.getHombresInvolucrados() + hombres);
        if (mujeres != null) subactividad.setMujeresInvolucradas(subactividad.getMujeresInvolucradas() + mujeres);
            
        if (file != null && !file.isEmpty()) {
            try {
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();
                
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, file.getBytes());
                
                SubactividadArchivo archivo = SubactividadArchivo.builder()
                    .nombre(file.getOriginalFilename())
                    .url("/uploads/" + fileName)
                    .subactividad(subactividad)
                    .build();
                    
                subactividad.addArchivo(archivo);
                archivoRepository.save(archivo);
            } catch (IOException e) {
                throw new RuntimeException("No se pudo guardar el archivo localmente", e);
            }
        }
        
        return toDTO(subactividadRepository.save(subactividad));
    }

    public SubactividadResponseDTO toDTO(Subactividad s) {
        return new SubactividadResponseDTO(
            s.getId(),
            s.getNombre(),
            s.getResponsable().getNombres() + " " + s.getResponsable().getApellidos(),
            s.getPresupuesto(),
            s.getHombresInvolucrados(),
            s.getMujeresInvolucradas(),
            s.getFechaInicio(),
            s.getFechaFin(),
            s.getEstado(),
            s.getDescripcion(),
            s.getArchivosEvidencia().stream()
                .map(a -> new SubactividadArchivoResponseDTO(a.getId(), a.getNombre(), a.getUrl()))
                .collect(Collectors.toList()),
            s.getCofinanciamientos().stream()
                .map(c -> new SubactividadCofinanciamientoResponseDTO(c.getActividadOrigen().getId(), c.getMonto()))
                .collect(Collectors.toList())
        );
    }
}
