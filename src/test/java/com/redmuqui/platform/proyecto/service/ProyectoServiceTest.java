package com.redmuqui.platform.proyecto.service;

import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.ejetematico.repository.EjeTematicoRepository;
import com.redmuqui.platform.macroregion.repository.MacroregionRepository;
import com.redmuqui.platform.proyecto.dto.ProyectoCreateDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoUpdateDTO;
import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.mapper.ProyectoMapper;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock private ProyectoRepository proyectoRepository;
    @Mock private MacroregionRepository macroregionRepository;
    @Mock private EjeTematicoRepository ejeTematicoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TerritorioRepository territorioRepository;
    @Mock private ProyectoMapper mapper;

    @InjectMocks private ProyectoService service;

    @Test
    void listarUsaSpecificationConFiltros() {
        Pageable pageable = PageRequest.of(0, 10);
        when(proyectoRepository.findAll(anySpecification(), eq(pageable))).thenReturn(Page.empty());

        Page<?> result = service.listar("PRY", EstadoProyecto.EN_CURSO, 1L, 2L, pageable);

        assertThat(result.getTotalElements()).isZero();
        verify(proyectoRepository).findAll(anySpecification(), eq(pageable));
    }

    @Test
    void crearRechazaFechaFinAnteriorAInicio() {
        ProyectoCreateDTO dto = new ProyectoCreateDTO(
            "Proyecto prueba",
            "PRY-TEST-001",
            "Descripcion",
            "Objetivo",
            LocalDate.of(2026, 5, 12),
            LocalDate.of(2026, 5, 11),
            EstadoProyecto.PENDIENTE,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
        when(proyectoRepository.existsByCodigoInternoIgnoreCase(dto.codigoInterno())).thenReturn(false);

        assertThatThrownBy(() -> service.crear(dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("fecha de fin");
        verify(proyectoRepository, never()).save(any());
    }

    @Test
    void crearRechazaTerritoriosInexistentes() {
        ProyectoCreateDTO dto = new ProyectoCreateDTO(
            "Proyecto prueba",
            "PRY-TEST-002",
            "Descripcion",
            "Objetivo",
            LocalDate.of(2026, 5, 12),
            LocalDate.of(2026, 5, 20),
            EstadoProyecto.PENDIENTE,
            null,
            null,
            null,
            null,
            null,
            null,
            Set.of(999L)
        );
        when(proyectoRepository.existsByCodigoInternoIgnoreCase(dto.codigoInterno())).thenReturn(false);
        when(territorioRepository.findAllById(dto.idTerritorios())).thenReturn(List.of());

        assertThatThrownBy(() -> service.crear(dto))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Territorio");
        verify(proyectoRepository, never()).save(any());
    }

    @Test
    void actualizarRechazaAvanceFueraDeRango() {
        Proyecto proyecto = Proyecto.builder()
            .nombre("Proyecto existente")
            .codigoInterno("PRY-EXISTENTE")
            .fechaInicio(LocalDate.of(2026, 5, 1))
            .estado(EstadoProyecto.EN_CURSO)
            .build();
        ProyectoUpdateDTO dto = new ProyectoUpdateDTO(
            "Proyecto actualizado",
            "Descripcion",
            "Objetivo",
            LocalDate.of(2026, 5, 12),
            LocalDate.of(2026, 5, 20),
            EstadoProyecto.EN_CURSO,
            null,
            101.0,
            null,
            null,
            null,
            null,
            null,
            null
        );
        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));

        assertThatThrownBy(() -> service.actualizar(1L, dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("avance");
    }

    @SuppressWarnings("unchecked")
    private Specification<Proyecto> anySpecification() {
        return any(Specification.class);
    }
}
