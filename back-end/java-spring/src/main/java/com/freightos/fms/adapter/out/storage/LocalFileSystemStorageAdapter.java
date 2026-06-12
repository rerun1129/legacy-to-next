package com.freightos.fms.adapter.out.storage;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.attachment.port.out.StoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * StoragePort 로컬 파일시스템 구현체.
 * 향후 S3 어댑터로 교체 가능한 치환점.
 * 경로 탈출 공격 방어: 모든 경로는 basePath 하위인지 검증한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalFileSystemStorageAdapter implements StoragePort {

    private final LocalStorageProperties storageProperties;

    @Override
    public void store(String storageKey, InputStream content, long contentLength) {
        Path target = resolveAndValidate(storageKey);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("첨부파일 저장 실패: storageKey=" + storageKey, e);
        }
    }

    @Override
    public InputStream load(String storageKey) {
        Path target = resolveAndValidate(storageKey);
        if (!Files.exists(target)) {
            throw new ResourceNotFoundException("첨부파일", storageKey);
        }
        try {
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new UncheckedIOException("첨부파일 읽기 실패: storageKey=" + storageKey, e);
        }
    }

    @Override
    public boolean delete(String storageKey) {
        Path target = resolveAndValidate(storageKey);
        try {
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("첨부파일 삭제 실패: storageKey={}, error={}", storageKey, e.getMessage());
            return false;
        }
    }

    @Override
    public List<StoredObject> list() {
        Path basePath = storageProperties.resolvedBasePath();
        if (!Files.exists(basePath)) {
            return List.of();
        }
        List<StoredObject> result = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(basePath)) {
            walk.filter(Files::isRegularFile).forEach(file -> {
                try {
                    Instant lastModified = Files.getLastModifiedTime(file).toInstant();
                    // basePath 상대 경로를 '/' 구분자로 변환
                    String key = basePath.relativize(file).toString().replace('\\', '/');
                    result.add(new StoredObject(key, lastModified));
                } catch (IOException e) {
                    log.warn("파일 속성 조회 실패: path={}, error={}", file, e.getMessage());
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException("스토리지 목록 조회 실패", e);
        }
        return result;
    }

    /**
     * storageKey를 basePath 기준 절대 경로로 변환하고,
     * basePath 상위 탈출 여부를 검증한다 (경로 탈출 공격 방어).
     */
    private Path resolveAndValidate(String storageKey) {
        Path basePath = storageProperties.resolvedBasePath();
        Path resolved = basePath.resolve(storageKey).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new IllegalArgumentException("허용되지 않은 파일 경로: storageKey=" + storageKey);
        }
        return resolved;
    }
}
