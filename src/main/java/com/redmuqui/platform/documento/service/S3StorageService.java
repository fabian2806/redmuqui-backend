package com.redmuqui.platform.documento.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class S3StorageService {

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    private final S3Presigner s3Presigner;

    public String generarUrlDescarga(String keyS3, String nombreArchivo) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(keyS3)
                .responseContentDisposition("attachment; filename=\"" + nombreArchivo + "\"")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

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

    @PostConstruct
    public void verificarConfiguracion() {
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");

        System.out.println("S3 bucket usado: " + bucket);
        System.out.println("S3 region usada: " + region);
        System.out.println("AWS_ACCESS_KEY_ID leído: " +
                (accessKey == null ? "null" : accessKey.substring(0, Math.min(4, accessKey.length())) + "****")
        );
    }
}