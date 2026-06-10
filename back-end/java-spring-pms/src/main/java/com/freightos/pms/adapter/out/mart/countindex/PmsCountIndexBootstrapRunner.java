package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.document.PmsMartSyncState;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 기동 시 Count Index가 미완료 상태일 때 백그라운드로 자가치유 빌드를 실행한다.
 *
 * <p>조건:
 * <ol>
 *   <li>count-index.enabled=true
 *   <li>{p}:meta.complete 부재 (인덱스 미완료)
 *   <li>pms_bl_mart 동기화 이력 존재 (Mart 빌드 완료 상태)
 * </ol>
 *
 * <p>PmsMartBootstrapRunner(Order=LOWEST_PRECEDENCE) 이후 실행을 보장하기 위해
 * Order(LOWEST_PRECEDENCE - 1)를 사용한다.
 * 실패 시 warn 로그만 남기고 Mongo 폴백을 유지한다.
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@ConditionalOnProperty(prefix = "pms.mart.count-index", name = "enabled", havingValue = "true")
class PmsCountIndexBootstrapRunner implements ApplicationRunner {

    private final PmsCountIndexBulkBuilder bulkBuilder;
    private final MongoTemplate mongoTemplate;
    private final PmsMartProperties props;
    private final RedisTemplate<String, byte[]> redisTemplate;

    private final ExecutorService bootstrapExec = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "pms-count-index-bootstrap");
        t.setDaemon(true);
        return t;
    });

    PmsCountIndexBootstrapRunner(
            PmsCountIndexBulkBuilder bulkBuilder,
            MongoTemplate mongoTemplate,
            PmsMartProperties props,
            RedisTemplate<String, byte[]> redisTemplate) {
        this.bulkBuilder   = bulkBuilder;
        this.mongoTemplate = mongoTemplate;
        this.props         = props;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String prefix = props.getCountIndex().getKeyPrefix();

        // meta.complete 존재 여부 확인 — 이미 완료된 경우 건너뜀
        try {
            Object complete = redisTemplate.opsForHash().get(
                PmsCountIndexKeys.meta(prefix), PmsCountIndexKeys.META_COMPLETE);
            if ("1".equals(parseString(complete))) {
                log.info("Count Index 이미 완료 상태 → bootstrap 건너뜀.");
                return;
            }
        } catch (Exception e) {
            log.warn("Count Index bootstrap: Redis 연결 확인 실패 → 건너뜀. {}", e.toString());
            return;
        }

        // Mart 빌드 완료 여부 확인
        PmsMartSyncState state = mongoTemplate.findOne(
            Query.query(Criteria.where("_id").is("pms_bl_mart")), PmsMartSyncState.class);
        if (state == null || state.getLastFullRebuildAt() == null) {
            log.info("Count Index bootstrap: Mart 미빌드 상태 → 건너뜀(full rebuild 완료 후 자동 실행됨).");
            return;
        }

        log.info("Count Index 미완료 + Mart 완료 → 백그라운드 bulk 빌드 시작.");
        bootstrapExec.submit(() -> {
            try {
                bulkBuilder.rebuildFromMart();
                log.info("Count Index 자동 bulk 빌드 완료.");
            } catch (Exception e) {
                log.warn("Count Index 자동 빌드 실패 — Mongo 폴백 유지: {}", e.toString());
            }
        });
    }

    @PreDestroy
    void shutdown() {
        bootstrapExec.shutdownNow();
    }

    private static String parseString(Object val) {
        if (val == null) return null;
        if (val instanceof byte[] b) return new String(b, StandardCharsets.UTF_8);
        return val.toString();
    }
}
