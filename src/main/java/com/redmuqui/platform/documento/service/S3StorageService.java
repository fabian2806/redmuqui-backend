package com.redmuqui.platform.documento.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

/**
 * OWASP A05 – Security Misconfiguration:
 *   Eliminado @PostConstruct que imprimía con System.out.println() el nombre del
 *   bucket S3 y un prefijo de la AWS_ACCESS_KEY_ID en stdout. En contenedores/ECS
 *   esto queda en logs de infraestructura accesibles. Se reemplaza por log.debug().
 *
 * OWASP A03 – Injection (Header Injection):
 *   El Content-Disposition se construye con el nombre del archivo. Si nombreArchivo
 *   contiene caracteres de control o comillas, puede romper la cabecera.
 *   Se sanitiza el nombre antes de usarlo en el header.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    private final S3Presigner s3Presigner;

    public String generarUrlDescarga(String keyS3, String nombreArchivo) {
        // OWASP A03: sanitizar nombre para evitar header injection en Content-Disposition.
        String nombreSanitizado = sanitizarNombreArchivo(nombreArchivo);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(keyS3)
                // RFC 5987: usar filename* para caracteres no ASCII; aquí simplificado.
                .responseContentDisposition("attachment; filename=\"" + nombreSanitizado + "\"")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    public String subirArchivo(MultipartFile archivo, String key) {
        try (S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .build()) {

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(archivo.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize())
            );

            return key;

        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo para subirlo a S3.", e);
        }
    }

    public String construirUrl(String key) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    /**
     * OWASP A05: reemplaza System.out.println por log.debug() que solo aparece
     * si logging.level está en DEBUG, y nunca en producción (nivel WARN/INFO).
     *
     * NUNCA loguear credenciales AWS completas, ni siquiera prefijos.
     */
    // @PostConstruct — ELIMINADO (era un System.out.println con info de infraestructura)
    // Si necesitas verificar la config al arrancar, usa log.debug:
    //
    // @PostConstruct
    // public void verificarConfiguracion() {
    //     log.debug("[CONFIG] S3 bucket={} region={}", bucket, region);
    // }

    /**
     * Elimina caracteres problemáticos en nombres de archivo para evitar
     * header injection en el valor de Content-Disposition.
     */
    private String sanitizarNombreArchivo(String nombre) {
        if (nombre == null) return "archivo";
        // Eliminar comillas, saltos de línea y caracteres de control.
        return nombre
                .replaceAll("[\"\\r\\n\\\\]", "_")
                .replaceAll("[\\x00-\\x1F\\x7F]", "_")
                .trim();
    }
}