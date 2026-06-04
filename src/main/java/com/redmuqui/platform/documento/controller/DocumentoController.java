package com.redmuqui.platform.documento.controller;

import com.redmuqui.platform.common.dto.PageResponse;
import com.redmuqui.platform.documento.dto.ArchivoDTO;
import com.redmuqui.platform.documento.dto.DocumentoCreateDTO;
import com.redmuqui.platform.documento.dto.DocumentoResponseDTO;
import com.redmuqui.platform.documento.dto.EnlaceDocumentoDTO;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.service.ArchivoService;
import com.redmuqui.platform.documento.service.DocumentoService;
import com.redmuqui.platform.documento.service.EnlaceDocumentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@Tag(name = "Documentos")
public class DocumentoController {

    private final DocumentoService service;
    private final ArchivoService archivoService;
    private final EnlaceDocumentoService enlaceService;

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

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('DOCUMENTOS_VALIDATE')")
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
    public ResponseEntity<ArchivoDTO> adjuntarArchivo(
        @PathVariable Long id,
        @RequestPart("archivo") MultipartFile archivo,
        @RequestParam(required = false) String descripcion
    ) {
        return ResponseEntity.ok(archivoService.adjuntar(id, archivo, descripcion));
    }

    @GetMapping("/{id}/archivos/{idArchivo}/descargar")
    @PreAuthorize("hasAuthority('DOCUMENTOS_READ')")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long id, @PathVariable Long idArchivo) {
        ArchivoDTO archivo = archivoService.obtener(id, idArchivo);
        Resource resource = archivoService.cargarComoResource(id, idArchivo);
        String nombre = archivo.nombreArchivo() != null ? archivo.nombreArchivo().replace("\"", "") : "archivo";
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
            .body(resource);
    }

    @DeleteMapping("/{id}/archivos/{idArchivo}")
    @PreAuthorize("hasAuthority('DOCUMENTOS_UPDATE')")
    public ResponseEntity<Void> eliminarArchivo(@PathVariable Long id, @PathVariable Long idArchivo) {
        archivoService.eliminar(id, idArchivo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/enlaces")
    @PreAuthorize("hasAuthority('DOCUMENTOS_READ')")
    public ResponseEntity<List<EnlaceDocumentoDTO>> listarEnlaces(@PathVariable Long id) {
        return ResponseEntity.ok(enlaceService.listarPorDocumento(id));
    }

    @PostMapping("/{id}/enlaces")
    @PreAuthorize("hasAuthority('DOCUMENTOS_UPDATE')")
    public ResponseEntity<EnlaceDocumentoDTO> registrarEnlace(
        @PathVariable Long id,
        @Valid @RequestBody EnlaceDocumentoDTO dto
    ) {
        return ResponseEntity.ok(enlaceService.registrar(id, dto));
    }

    @DeleteMapping("/{id}/enlaces/{idEnlace}")
    @PreAuthorize("hasAuthority('DOCUMENTOS_UPDATE')")
    public ResponseEntity<Void> eliminarEnlace(@PathVariable Long id, @PathVariable Long idEnlace) {
        enlaceService.eliminar(id, idEnlace);
        return ResponseEntity.noContent().build();
    }
}
