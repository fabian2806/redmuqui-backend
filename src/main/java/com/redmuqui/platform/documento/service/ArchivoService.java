package com.redmuqui.platform.documento.service;

import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.documento.dto.ArchivoDTO;
import com.redmuqui.platform.documento.entity.Archivo;
import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.documento.repository.ArchivoRepository;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArchivoService {

    private static final long MAX_SIZE_BYTES = 20L * 1024L * 1024L;
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("pdf", "docx", "xlsx");

    private final ArchivoRepository archivoRepository;
    private final DocumentoRepository documentoRepository;

    @Value("${app.storage.documentos-path:uploads/documentos}")
    private String documentosStoragePath;

    @Transactional(readOnly = true)
    public List<ArchivoDTO> listarPorDocumento(Long idDocumento) {
        asegurarDocumentoExiste(idDocumento);
        return archivoRepository.findByDocumentoIdOrderByFechaCreacionDesc(idDocumento).stream()
            .map(this::toDTO)
            .toList();
    }

    @Transactional
    public ArchivoDTO adjuntar(Long idDocumento, MultipartFile archivo, String descripcion) {
        Documento documento = asegurarDocumentoExiste(idDocumento);
        validarArchivo(archivo);

        String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename() != null
            ? archivo.getOriginalFilename()
            : "archivo");
        String extension = obtenerExtension(nombreOriginal);
        String nombreAlmacenado = UUID.randomUUID() + "." + extension;
        String rutaRelativa = idDocumento + "/" + nombreAlmacenado;

        try {
            Path carpetaDocumento = storageRoot().resolve(String.valueOf(idDocumento)).normalize();
            Files.createDirectories(carpetaDocumento);
            Path destino = carpetaDocumento.resolve(nombreAlmacenado).normalize();
            if (!destino.startsWith(carpetaDocumento)) {
                throw new BusinessException("Ruta de archivo no permitida");
            }
            archivo.transferTo(destino);
        } catch (IOException ex) {
            throw new BusinessException("No se pudo almacenar el archivo adjunto", ex);
        }

        Archivo adjunto = Archivo.builder()
            .nombre(nombreOriginal)
            .url(rutaRelativa)
            .extension(extension)
            .descripcion(descripcion != null ? descripcion.trim() : null)
            .tamanioArchivo(archivo.getSize())
            .documento(documento)
            .build();
        return toDTO(archivoRepository.save(adjunto));
    }

    @Transactional
    public void eliminar(Long idDocumento, Long idArchivo) {
        asegurarDocumentoExiste(idDocumento);
        Archivo archivo = buscarArchivoDelDocumento(idDocumento, idArchivo);
        archivoRepository.delete(archivo);
        try {
            Files.deleteIfExists(storageRoot().resolve(archivo.getUrl()).normalize());
        } catch (IOException ex) {
            throw new BusinessException("No se pudo eliminar el archivo almacenado", ex);
        }
    }

    @Transactional(readOnly = true)
    public Resource cargarComoResource(Long idDocumento, Long idArchivo) {
        asegurarDocumentoExiste(idDocumento);
        Archivo archivo = buscarArchivoDelDocumento(idDocumento, idArchivo);
        try {
            Path ruta = storageRoot().resolve(archivo.getUrl()).normalize();
            if (!ruta.startsWith(storageRoot())) {
                throw new BusinessException("Ruta de archivo no permitida");
            }
            Resource resource = new UrlResource(ruta.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Archivo almacenado", idArchivo);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new BusinessException("No se pudo leer el archivo almacenado", ex);
        }
    }

    @Transactional(readOnly = true)
    public ArchivoDTO obtener(Long idDocumento, Long idArchivo) {
        asegurarDocumentoExiste(idDocumento);
        return toDTO(buscarArchivoDelDocumento(idDocumento, idArchivo));
    }

    private Documento asegurarDocumentoExiste(Long idDocumento) {
        return documentoRepository.findById(idDocumento)
            .orElseThrow(() -> new ResourceNotFoundException("Documento", idDocumento));
    }

    private Archivo buscarArchivoDelDocumento(Long idDocumento, Long idArchivo) {
        Archivo archivo = archivoRepository.findById(idArchivo)
            .orElseThrow(() -> new ResourceNotFoundException("Archivo", idArchivo));
        Long documentoId = archivo.getDocumento() != null ? archivo.getDocumento().getId() : null;
        if (!idDocumento.equals(documentoId)) {
            throw new ResourceNotFoundException("Archivo", idArchivo);
        }
        return archivo;
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("El archivo adjunto no puede estar vacio");
        }
        if (archivo.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException("El archivo adjunto no debe exceder los 20 MB");
        }
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = obtenerExtension(nombreOriginal);
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new BusinessException("Formato de archivo no permitido. Solo se aceptan PDF, DOCX y XLSX");
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            throw new BusinessException("Formato de archivo no permitido. Solo se aceptan PDF, DOCX y XLSX");
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1)
            .toLowerCase(Locale.ROOT)
            .trim();
    }

    private Path storageRoot() {
        return Paths.get(documentosStoragePath).toAbsolutePath().normalize();
    }

    private ArchivoDTO toDTO(Archivo a) {
        return new ArchivoDTO(
            a.getId(),
            a.getNombre(),
            a.getUrl(),
            a.getExtension(),
            a.getDescripcion(),
            a.getTamanioArchivo(),
            a.getFechaCreacion(),
            a.getDocumento() != null ? a.getDocumento().getId() : null
        );
    }
}
