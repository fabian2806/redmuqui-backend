package com.redmuqui.platform.documento.controller;

import com.redmuqui.platform.common.dto.PageResponse;
import com.redmuqui.platform.documento.dto.ArchivoDTO;
import com.redmuqui.platform.documento.dto.DocumentoCreateDTO;
import com.redmuqui.platform.documento.dto.DocumentoResponseDTO;
import com.redmuqui.platform.documento.dto.DocumentoUpdateDTO;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.service.ArchivoService;
import com.redmuqui.platform.documento.service.DocumentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@Tag(name = "Documentos")
public class DocumentoController {

    private final DocumentoService service;
    private final ArchivoService archivoService;

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENTOS_READ')")
    public ResponseEntity<PageResponse<DocumentoResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(service.listar(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENTOS_READ')")
    public ResponseEntity<DocumentoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOCUMENTOS_CREATE')")
    public ResponseEntity<DocumentoResponseDTO> crear(@Valid @RequestBody DocumentoCreateDTO dto) {
        DocumentoResponseDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/documentos/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENTOS_UPDATE')")
    public ResponseEntity<DocumentoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DocumentoCreateDTO dto
    ) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('DOCUMENTOS_UPDATE','DOCUMENTOS_VALIDATE')")
    public ResponseEntity<DocumentoResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam EstadoDocumento estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @GetMapping("/{id}/archivos")
    @PreAuthorize("hasAuthority('DOCUMENTOS_READ')")
    public ResponseEntity<List<ArchivoDTO>> listarArchivos(@PathVariable Long id) {
        return ResponseEntity.ok(archivoService.listarPorDocumento(id));
    }

    @PostMapping("/{id}/archivos")
    @PreAuthorize("hasAuthority('DOCUMENTOS_UPDATE')")
    public ResponseEntity<ArchivoDTO> agregarArchivo(@PathVariable Long id, @Valid @RequestBody ArchivoDTO dto) {
        return ResponseEntity.ok(archivoService.crear(id, dto));
    }


}
