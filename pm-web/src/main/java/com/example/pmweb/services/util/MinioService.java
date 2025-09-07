package com.example.pmweb.services.util;

import io.minio.*;
import io.minio.errors.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class MinioService {
    @Getter
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinioService(@Value("${minio.url}") String url,
                        @Value("${minio.access-key}") String accessKey,
                        @Value("${minio.secret-key}") String secretKey) {
        this.minioClient = MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
    }
    public String uploadFile(MultipartFile file, String objectName) {
        final String filenameForStoring = getNewFileName(objectName);

        try {
            checkBucketExistence();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                            .object(filenameForStoring)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return objectName;
        } catch(Exception e) {
            throw new RuntimeException("Something went wrong while uploading file from web storage", e);
        }
    }

    public byte[] downloadFile(String objectName) {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName + ".apk")
                        .build())){

            byte[] fileBytes = stream.readAllBytes();
            log.info("File successfully downloaded: {}", objectName);
            return fileBytes;
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong while downloading file from web storage", e);
        }
    }

    public void deleteFile(String objectName) {
        String filenameForStoring = getNewFileName(objectName);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filenameForStoring)
                            .build());
            log.info("File successfully deleted: {}", objectName);
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong while deleting file from web storage", e);
        }
    }

    private String getNewFileName(String objectName) {
        return objectName.trim() + ".apk";
    }

    private void checkBucketExistence() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
                 ServerException | XmlParserException e) {
            throw new RuntimeException("Something went wrong on web storage: " + e.getMessage(), e);
        }
    }
}
