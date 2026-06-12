package com.freightos.fms.application.attachment.port.out;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

/**
 * 파일 스토리지 아웃바운드 포트.
 * 현재 구현체는 LocalFileSystemStorageAdapter이며,
 * 향후 S3 어댑터로 교체 가능하도록 설계된 치환점이다.
 */
public interface StoragePort {

    void store(String storageKey, InputStream content, long contentLength);

    /**
     * 파일을 읽어 InputStream을 반환한다.
     * 파일이 존재하지 않으면 도메인 예외를 던진다.
     */
    InputStream load(String storageKey);

    /**
     * 파일을 삭제하고 성공 여부를 반환한다.
     * 삭제 실패 시 예외를 던지지 않고 false를 반환한다 — 고아 파일은 배치가 회수한다.
     */
    boolean delete(String storageKey);

    /** 스토리지에 존재하는 모든 객체 목록 (고아 파일 정리 배치용). */
    List<StoredObject> list();

    /** S3 ListObjectsV2 호환 시그니처. */
    record StoredObject(String key, Instant lastModified) {
    }
}
