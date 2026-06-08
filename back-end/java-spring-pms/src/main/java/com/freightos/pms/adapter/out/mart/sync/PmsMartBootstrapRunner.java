package com.freightos.pms.adapter.out.mart.sync;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.PmsMartReadiness;
import com.freightos.pms.application.mart.port.out.PmsMartSyncPort;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 기동 시 Mart 상태를 확인해 필요하면 백그라운드 full rebuild를 실행한다.
 *
 * 실행 순서:
 *  1. readState()로 lastFullRebuildAt 존재 여부 확인.
 *  2. 이미 빌드되어 있으면 즉시 readiness.markReady() 후 반환 — 빌드 비용 없음.
 *  3. 비어 있고 autoRebuild=true이면 daemon 스레드에서 rebuildFull()을 비동기 실행.
 *     그 동안 readiness=false → OLTP 폴백 유지.
 *  4. 비어 있고 autoRebuild=false이면 warn 로그만 남기고 반환(OLTP 폴백 유지).
 *
 * @Order(LOWEST_PRECEDENCE)로 PmsMartIndexInitializer 등 인덱스 초기화 이후에 실행된다.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class PmsMartBootstrapRunner implements ApplicationRunner {

    private final PmsMartSyncPort etl;
    private final PmsMartReadiness readiness;
    private final PmsMartProperties props;

    // 단일 스레드 daemon 풀 — 애플리케이션 생존 여부와 무관하게 JVM이 종료되면 함께 종료된다.
    private final ExecutorService bootstrapExec = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "pms-mart-bootstrap");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void run(ApplicationArguments args) {
        boolean alreadyBuilt = etl.readState().getLastFullRebuildAt() != null;
        if (alreadyBuilt) {
            readiness.markReady();
            log.info("Mart 이미 빌드 완료 → ready. OLTP 폴백 없이 Mart 경로 사용.");
            return;
        }

        if (!props.getBootstrap().isAutoRebuild()) {
            log.warn("Mart 비어 있음 + auto-rebuild=false → OLTP 폴백 유지. 수동 rebuild API를 호출하세요.");
            // readiness=false 유지 — Mart 미사용
            return;
        }

        log.info("Mart 비어 있음 → 백그라운드 full rebuild 시작. 빌드 완료 전까지 OLTP 폴백 유지.");
        // run()은 즉시 반환해 기동을 차단하지 않는다.
        bootstrapExec.submit(() -> {
            try {
                etl.rebuildFull();
                readiness.markReady();
                log.info("Mart 자동 full rebuild 완료 → ready.");
            } catch (Exception e) {
                log.error("Mart 자동 rebuild 실패. OLTP 폴백 유지.", e);
                // readiness=false 유지 — 실패 시에도 OLTP가 정상 서비스
            }
        });
    }

    @PreDestroy
    void shutdown() {
        bootstrapExec.shutdownNow();
    }
}
