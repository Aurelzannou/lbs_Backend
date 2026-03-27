package com.App.lbs_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    public String getUrl() {
        return url;
    }

    public MinioProperties setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public MinioProperties setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public MinioProperties setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getBucketName() {
        return bucketName;
    }

    public MinioProperties setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }
}
