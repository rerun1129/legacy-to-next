package com.freightos.fms.adapter.out.storage;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;
import java.time.Duration;

/**
 * S3 스토리지 모드 설정.
 * fms.storage.mode=s3 일 때만 활성화된다.
 *
 * ⚠️ key-prefix는 최초 설정 후 불변.
 * 변경 시 기존 객체가 list()에서 빠져 unmanaged 잔존 상태가 되므로
 * prefix 변경이 필요하면 기존 객체 전체 마이그레이션 후 적용한다.
 *
 * 기존 StorageConfig는 이 클래스와 독립적으로 유지된다 (무변경).
 * S3Client는 lazy — S3 다운 중에도 부팅 가능, 즉시 스필오버 동작.
 */
@Configuration
@ConditionalOnProperty(name = "fms.storage.mode", havingValue = "s3")
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3StorageConfig {

    /**
     * S3Client 빈.
     * 자격증명은 AWS default credentials chain에 위임한다
     * (환경변수 → ~/.aws/credentials → IAM 인스턴스 프로파일 순서).
     * 코드에 키를 하드코딩하지 않으며, 루트 .env의 AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY가
     * compose를 통해 환경변수로 주입된다.
     */
    @Bean
    public S3Client s3Client(S3StorageProperties properties) {
        if (properties.getBucket() == null || properties.getBucket().isBlank()) {
            throw new IllegalStateException(
                    "fms.storage.mode=s3 이지만 fms.storage.s3.bucket 이 설정되지 않았습니다. " +
                    "FMS_S3_BUCKET 환경변수를 확인하세요.");
        }

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .overrideConfiguration(c -> c
                        .apiCallTimeout(Duration.ofSeconds(60))
                        .apiCallAttemptTimeout(Duration.ofSeconds(55)))
                .httpClientBuilder(ApacheHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(2))
                        .socketTimeout(Duration.ofSeconds(30)));

        String endpoint = properties.getEndpoint();
        if (endpoint != null && !endpoint.isBlank()) {
            // MinIO 또는 로컬 드릴용 — path-style 강제(MinIO는 virtual-hosted-style 미지원)
            builder.endpointOverride(URI.create(endpoint))
                   .forcePathStyle(true);
        }

        return builder.build();
    }

    @Bean
    public S3StorageAdapter s3StorageAdapter(S3Client s3Client, S3StorageProperties properties) {
        return new S3StorageAdapter(s3Client, properties);
    }

    @Bean
    @Primary
    public FailoverStoragePort failoverStoragePort(
            CircuitBreakerRegistry registry,
            S3StorageAdapter s3StorageAdapter,
            LocalFileSystemStorageAdapter localFileSystemStorageAdapter) {
        return new FailoverStoragePort(registry, s3StorageAdapter, localFileSystemStorageAdapter);
    }

    @Bean
    public AttachmentS3Promoter attachmentS3Promoter(
            CircuitBreakerRegistry registry,
            S3StorageAdapter s3StorageAdapter,
            LocalFileSystemStorageAdapter localFileSystemStorageAdapter) {
        return new AttachmentS3Promoter(registry, s3StorageAdapter, localFileSystemStorageAdapter);
    }
}
