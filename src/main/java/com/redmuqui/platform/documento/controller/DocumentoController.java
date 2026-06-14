package com.redmuqui.platform.documento.controller;

import com.redmuqui.platform.common.dto.PageResponse;
import com.redmuqui.platform.documento.dto.*;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.service.ArchivoService;
import com.redmuqui.platform.documento.service.DocumentoComentarioService;
import com.redmuqui.platform.documento.service.DocumentoService;
import com.redmuqui.platform.documento.service.DocumentoVersionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@Tag(name = "Documentos")
public class DocumentoController {

    private final DocumentoService service;
    private final ArchivoService archivoService;
    private final DocumentoVersionService versionService;
    private final DocumentoComentarioService comentarioService;

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENTOS_READ')")
    public ResponseEntity<PageResponse<DocumentoResponseDTO>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long idProyecto,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) EstadoDocumento estado,
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(
            service.listar(q, idProyecto, fechaDesde, fechaHasta, estado, pageable)
        ));
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
            @Valid @RequestBody DocumentoUpdateDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('DOCUMENTOS_UPDATE','DOCUMENTOS_VALIDATE')")
    public ResponseEntity<DocumentoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoDocumento estado
    ) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @GetMapping("/{id}/archivos")
    @PreAuthorize("hasAuthority('DOCUMENTOS_READ')")
    public ResponseEntity<List<ArchivoDTO>> listarArchivos(@PathVariable Long id) {
        return ResponseEntity.ok(archivoService.listarPorDocumento(id));
    }

    @GetMapping("/{id}/versiones")
    @PreAuthorize("hasAuthority('DOCUMENTOS_READ')")
    public ResponseEntity<List<DocumentoVersionDTO>> listarVersiones(@PathVariable Long id) {
        service.obtener(id);
        return ResponseEntity.ok(versionService.listar(id));
    }

    @GetMapping("/{id}/comentarios")
    @PreAuthorize("hasAuthority('DOCUMENTOS_READ')")
    public ResponseEntity<PageResponse<DocumentoComentarioDTO>> listarComentarios(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(comentarioService.listar(id, pageable));
    }

    @PostMapping("/{id}/comentarios")
    @PreAuthorize("hasAnyAuthority('DOCUMENTOS_UPDATE','DOCUMENTOS_VALIDATE')")
    public ResponseEntity<DocumentoComentarioDTO> comentar(
            @PathVariable Long id,
            @Valid @RequestBody DocumentoComentarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comentarioService.crear(id, request));
    }

    @PostMapping(
            value = "/{id}/archivos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAuthority('DOCUMENTOS_UPDATE')")
    public ResponseEntity<ArchivoDTO> agregarArchivo(
            @PathVariable Long id,
            @RequestPart("archivo") MultipartFile archivo,
            @RequestParam(value = "descripcion", required = false) String descripcion
    ) {
        ArchivoDTO creado = archivoService.adjuntarArchivo(id, archivo, descripcion);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/{id}/archivos/{archivoId}/descarga")
    @PreAuthorize("hasAuthority('DOCUMENTOS_READ')")
    public ResponseEntity<ArchivoDescargaResponse> obtenerUrlDescargaArchivo(
            @PathVariable Long id,
            @PathVariable Long archivoId
    ) {
        String url = archivoService.generarUrlDescarga(id, archivoId);

        return ResponseEntity.ok(new ArchivoDescargaResponse(url));
    }
}
