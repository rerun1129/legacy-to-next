package com.freightos.fms.adapter.out.storage;

import com.freightos.fms.application.attachment.port.out.BlAttachmentPort;
import com.freightos.fms.application.attachment.port.out.StoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * 고아 첨부파일 정리 스케줄러.
 * 스토리지에 존재하지만 메타DB에 없는 파일(grace 기간 경과)을 삭제한다.
 * 메타DB에만 있고 파일이 없는 dangling 메타는 WARN 로그만 남긴다.
 *
 * ⚠️ cleanup.enabled=false 면 승격(AttachmentS3Promoter)도 정지된다.
 * 승격은 이 스케줄러 run()의 선두에서 호출되므로 스케줄러 게이트를 공유한다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "fms.storage.cleanup.enabled", matchIfMissing = true)
public class AttachmentCleanupScheduler {

    private final StoragePort storagePort;
    private final BlAttachmentPort blAttachmentPort;
    private final Clock clock;
    private final AttachmentCleanupProperties cleanupProperties;
    private final AttachmentS3Promoter promoter;

    public AttachmentCleanupScheduler(
            StoragePort storagePort,
            BlAttachmentPort blAttachmentPort,
            Clock clock,
            AttachmentCleanupProperties cleanupProperties,
            ObjectProvider<AttachmentS3Promoter> promoterProvider) {
        this.storagePort = storagePort;
        this.blAttachmentPort = blAttachmentPort;
        this.clock = clock;
        this.cleanupProperties = cleanupProperties;
        // local 모드에서는 AttachmentS3Promoter 빈이 없으므로 null 허용
        this.promoter = promoterProvider.getIfAvailable();
    }

    @Scheduled(cron = "${fms.storage.cleanup.cron:0 0 3 * * *}")
    public void cleanOrphanFiles() {
        log.info("첨부파일 고아 정리 시작");

        Set<String> metaKeys = Set.copyOf(blAttachmentPort.findAllStorageKeys());

        // 승격은 list()보다 선행한다 — S3 승격 완료분이 로컬에서 제거된 후 list()를 수행해야
        // 승격된 파일이 고아로 오판되지 않는다.
        if (promoter != null) {
            try {
                promoter.promote(metaKeys);
            } catch (RuntimeException e) {
                log.warn("S3 승격 중 예외 발생 — sweep 계속: {}", e.toString());
            }
        }

        List<StoragePort.StoredObject> storageObjects;
        try {
            storageObjects = storagePort.list();
        } catch (RuntimeException e) {
            // list() 실패 시 부분 목록으로 고아를 오판하지 않도록 run 전체를 건너뜀
            log.warn("스토리지 목록 조회 실패 — 이번 주기 정리 건너뜀(고아 오판 방지): {}", e.toString());
            return;
        }

        Instant now = clock.instant();
        Duration grace = Duration.ofHours(cleanupProperties.getGraceHours());

        int deleted = 0;
        int dangling = 0;

        for (StoragePort.StoredObject obj : storageObjects) {
            if (!metaKeys.contains(obj.key())) {
                if (now.isAfter(obj.lastModified().plus(grace))) {
                    boolean ok = storagePort.delete(obj.key());
                    if (ok) {
                        deleted++;
                        log.debug("고아 파일 삭제: key={}", obj.key());
                    } else {
                        log.warn("고아 파일 삭제 실패 (다음 주기 재시도): key={}", obj.key());
                    }
                }
            }
        }

        // dangling 메타 탐지 — 메타는 있는데 파일이 없는 경우
        Set<String> storageKeys = storageObjects.stream()
                .map(StoragePort.StoredObject::key)
                .collect(java.util.stream.Collectors.toSet());
        for (String metaKey : metaKeys) {
            if (!storageKeys.contains(metaKey)) {
                log.warn("Dangling 첨부파일 메타 (파일 유실): storageKey={}", metaKey);
                dangling++;
            }
        }

        log.info("첨부파일 고아 정리 완료: deleted={}, dangling={}", deleted, dangling);
    }
}
