package com.redmuqui.platform.documento.service;

import com.redmuqui.platform.documento.dto.ArchivoDTO;
import com.redmuqui.platform.documento.entity.Archivo;
import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.repository.ArchivoRepository;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.common.audit.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArchivoService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "pdf",
            "docx",
            "xlsx",
            "jpeg",
            "png",
            "jpg"
    );

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final ArchivoRepository archivoRepository;
    private final DocumentoRepository documentoRepository;
    private final S3StorageService s3StorageService;
    private final AuthenticatedUserService authenticatedUserService;
    private final DocumentoVersionService documentoVersionService;

    @Transactional(readOnly = true)
    public List<ArchivoDTO> listarPorDocumento(Long documentoId) {
        verificarDocumentoExiste(documentoId);

        return archivoRepository.findByDocumentoId(documentoId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public ArchivoDTO adjuntarArchivo(Long documentoId, MultipartFile archivo, String descripcion) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Documento no encontrado."
                ));
        if (documento.getEstado() == EstadoDocumento.PUBLICADO) {
            throw new BusinessException(
                "Debe devolver el documento a revisión antes de cargar una nueva versión.");
        }

        validarArchivo(archivo);

        String nombreOriginal = archivo.getOriginalFilename();
        String extension = obtenerExtension(nombreOriginal);
        String nombreSeguro = limpiarNombreArchivo(nombreOriginal);

        String key = "documentos/" + documentoId + "/" + UUID.randomUUID() + "-" + nombreSeguro;

        String rutaS3 = s3StorageService.subirArchivo(archivo, key);
        String url = s3StorageService.construirUrl(rutaS3);

        Archivo nuevoArchivo = Archivo.builder()
                .nombre(nombreOriginal)
                .url(url)
                .extension(extension)
                .tipoContenido(archivo.getContentType())
                .descripcion(descripcion)
                .tamanioBytes(archivo.getSize())
                .numeroVersion(siguienteVersion(documento))
                .usuarioCarga(authenticatedUserService.obtenerUsuario())
                .documento(documento)
                .build();

        Archivo guardado = archivoRepository.save(nuevoArchivo);
        documento.setVersion(guardado.getNumeroVersion().doubleValue());
        documentoRepository.save(documento);
        documentoVersionService.registrar(documento, "Carga del archivo " + nombreOriginal);

        return toDTO(guardado);
    }

    /**
     * Este método queda solo si alguna parte antigua del sistema todavía lo usa.
     * Ya no debe usarse para subir archivos reales.
     */
    public ArchivoDTO crear(Long documentoId, ArchivoDTO dto) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Documento no encontrado."
                ));

        Archivo archivo = Archivo.builder()
                .nombre(dto.nombre())
                .url(dto.url())
                .extension(dto.extension())
                .tipoContenido(dto.tipoContenido())
                .descripcion(dto.descripcion())
                .tamanioBytes(dto.tamanioBytes() != null ? dto.tamanioBytes() : 0L)
                .numeroVersion(siguienteVersion(documento))
                .usuarioCarga(authenticatedUserService.obtenerUsuario())
                .documento(documento)
                .build();

        return toDTO(archivoRepository.save(archivo));
    }

    private void verificarDocumentoExiste(Long documentoId) {
        if (!documentoRepository.existsById(documentoId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Documento no encontrado."
            );
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe adjuntar un archivo."
            );
        }

        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El archivo no debe superar los 20 MB."
            );
        }

        String extension = obtenerExtension(archivo.getOriginalFilename());

        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten archivos PDF, DOCX y XLSX."
            );
        }

        String contentType = archivo.getContentType();

        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten archivos PDF, DOCX y XLSX."
            );
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }

        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1)
                .toLowerCase(Locale.ROOT);
    }

    private String limpiarNombreArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return "archivo";
        }

        String normalizado = Normalizer.normalize(nombreArchivo, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        return normalizado
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .toLowerCase(Locale.ROOT);
    }

    private String extraerKeyDesdeUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "El archivo no tiene una URL de S3 registrada."
            );
        }

        int index = url.indexOf(".amazonaws.com/");

        if (index == -1) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "La URL del archivo no tiene un formato válido de S3."
            );
        }

        return url.substring(index + ".amazonaws.com/".length());
    }

    private ArchivoDTO toDTO(Archivo archivo) {
        return new ArchivoDTO(
                archivo.getId(),
                archivo.getNombre(),
                archivo.getUrl(),
                archivo.getExtension(),
                archivo.getTipoContenido(),
                archivo.getDescripcion(),
                archivo.getTamanioBytes(),
                archivo.getNumeroVersion(),
                archivo.getUsuarioCarga() != null ? archivo.getUsuarioCarga().getId() : null,
                archivo.getUsuarioCarga() != null
                    ? (archivo.getUsuarioCarga().getNombres() + " " + archivo.getUsuarioCarga().getApellidos()).trim()
                    : null
        );
    }

    private int siguienteVersion(Documento documento) {
        return (int) Math.floor(documento.getVersion() == null ? 0D : documento.getVersion()) + 1;
    }

    public String generarUrlDescarga(Long documentoId, Long archivoId) {
        Archivo archivo = archivoRepository
                .findByIdAndDocumentoId(archivoId, documentoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Archivo no encontrado para el documento indicado."
                ));

        String keyS3 = extraerKeyDesdeUrl(archivo.getUrl());

        return s3StorageService.generarUrlDescarga(
                keyS3,
                archivo.getNombre()
        );
    }
}
