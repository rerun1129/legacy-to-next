package com.freightos.fms.adapter.out.storage;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.attachment.port.out.StoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * StoragePort S3 구현체.
 * S3Client를 직접 사용하며, 페이지네이터 대신 continuationToken 수동 루프를 사용한다
 * (mock 친화적이고 제어 흐름이 명확하다).
 */
@Slf4j
@RequiredArgsConstructor
public class S3StorageAdapter implements StoragePort {

    private final S3Client s3Client;
    private final S3StorageProperties properties;

    @Override
    public void store(String storageKey, InputStream content, long contentLength) {
        String objectKey = properties.normalizedKeyPrefix() + storageKey;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(content, contentLength));
    }

    /**
     * S3에서 객체를 읽어 InputStream을 반환한다.
     * 객체가 존재하지 않으면 ResourceNotFoundException을 던진다 (로컬 어댑터와 자구 동일).
     * CB ignoreExceptions 대상이므로 not-found는 CB failure 카운트에 집계되지 않는다.
     */
    @Override
    public InputStream load(String storageKey) {
        String objectKey = properties.normalizedKeyPrefix() + storageKey;
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();
        try {
            return s3Client.getObject(request);
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("첨부파일", storageKey);
        }
    }

    /**
     * S3 객체를 삭제한다.
     * S3 DeleteObject는 미존재 시에도 204를 반환하는 멱등 API이므로 항상 true를 반환한다.
     * SDK 예외는 포트 계약(예외 금지)에 따라 WARN 로그 후 false를 반환한다.
     */
    @Override
    public boolean delete(String storageKey) {
        String objectKey = properties.normalizedKeyPrefix() + storageKey;
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();
        try {
            s3Client.deleteObject(request);
            return true;
        } catch (SdkException e) {
            log.warn("S3 객체 삭제 실패: storageKey={}, error={}", storageKey, e.getMessage());
            return false;
        }
    }

    /**
     * S3 버킷 내 prefix 하위 객체 전체 목록을 반환한다.
     * continuationToken 수동 루프로 페이지네이션을 처리한다.
     * 반환 키는 prefix를 제거한 storageKey 형태다.
     */
    @Override
    public List<StoredObject> list() {
        String prefix = properties.normalizedKeyPrefix();
        List<StoredObject> result = new ArrayList<>();
        String continuationToken = null;

        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(properties.getBucket())
                    .prefix(prefix);
            if (continuationToken != null) {
                requestBuilder.continuationToken(continuationToken);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());

            for (S3Object s3Object : response.contents()) {
                String storageKey = s3Object.key().substring(prefix.length());
                result.add(new StoredObject(storageKey, s3Object.lastModified()));
            }

            continuationToken = response.isTruncated() ? response.nextContinuationToken() : null;
        } while (continuationToken != null);

        return result;
    }
}
