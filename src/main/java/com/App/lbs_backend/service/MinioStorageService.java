package com.App.lbs_backend.service;

import com.App.lbs_backend.config.MinioProperties;
import com.App.lbs_backend.dto.response.FileResponse;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class MinioStorageService {

    public static final String X_FILE_SIZE = "X-File-Size";
    private final MinioClient minioClient;
    private final MinioProperties properties;

    public MinioStorageService(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    public static void setHeaderFileSize(HttpServletResponse response, long length) {
        Pair<String, String> fileSize = formatFileSize(length);
        response.setHeader(X_FILE_SIZE, String.format("%s %s", fileSize.getFirst(), fileSize.getSecond()));
    }

    public static Pair<String, String> formatFileSize(long sizeInBytes) {
        if (sizeInBytes <= 0) return Pair.of("0", "B");
        final String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};
        int unitIndex = (int) (Math.log10(sizeInBytes) / Math.log10(1024));
        double sizeInUnit = sizeInBytes / Math.pow(1024, unitIndex);
        return Pair.of(String.format("%.2f", sizeInUnit), units[unitIndex]);
    }

    @Transactional
    public String uploadFile(final String directory, MultipartFile file) throws Exception {
        InputStream fileStream = file.getInputStream();
        String dir = directory == null || directory.isEmpty() ? "" : directory + "/";
        String safeUploadFileName = requireNonNull(file.getOriginalFilename()).toLowerCase();
        String fileName = String.format("%s%s-%s", dir, System.currentTimeMillis(), safeUploadFileName);

        PutObjectArgs bucketFileData = PutObjectArgs.builder()
                .bucket(bucketName())
                .object(fileName)
                .stream(fileStream, file.getSize(), -1)
                .contentType(file.getContentType())
                .build();
        minioClient.putObject(bucketFileData);

        return fileName;
    }

    public FileResponse downloadFile(String fileName) throws Exception {
        StatObjectArgs statArgs = StatObjectArgs.builder().bucket(bucketName()).object(fileName).build();
        StatObjectResponse stat = minioClient.statObject(statArgs);
        long length = stat.size();
        String type = stat.contentType();
        GetObjectArgs fileData = GetObjectArgs.builder().bucket(bucketName()).object(fileName).build();

        return new FileResponse(fileName, length, type, minioClient.getObject(fileData));
    }

    @Transactional
    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName())
                .object(fileName)
                .build());
    }

    @PostConstruct
    public void createBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName()).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName()).build());
            }
        } catch (Exception e) {
            log.error("Erreur lors de la vérification/création du bucket MinIO : {}", e.getMessage());
        }
    }

    private String bucketName() {
        return properties.getBucketName();
    }
}
