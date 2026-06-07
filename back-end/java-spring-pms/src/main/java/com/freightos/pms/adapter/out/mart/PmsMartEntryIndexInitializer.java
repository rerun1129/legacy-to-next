package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.adapter.out.mart.document.PmsDocDtEntryDocument;
import com.freightos.pms.adapter.out.mart.document.PmsPerfDtEntryDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * line-accel ON일 때만 등록되는 sidecar 컬렉션 인덱스 초기화기.
 * ensureIndex는 동일 정의 재호출 시 기존 인덱스를 유지하므로 멱등 안전.
 *
 * residual 방식(날짜로 IXSCAN 좁힌 뒤 FETCH 후 잔차필터)이므로
 * B/L 필터 필드 전용 인덱스는 불필요.
 *
 * pms_perfdt_entry: (flag, pd, blKey, blId, blType) — 날짜 3종 covered count/find
 * pms_docdt_entry : (perfPd|docDt, blKey, blId, blType) — 날짜 2종
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart.line-accel", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsMartEntryIndexInitializer implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        buildPerfDtIndexes();
        buildDocDtIndexes();
    }

    // ── pms_perfdt_entry ─────────────────────────────────────────────────────

    private void buildPerfDtIndexes() {
        IndexOperations ops = mongoTemplate.indexOps(PmsPerfDtEntryDocument.class);

        // basis 존재 플래그 × (pd, trailing) — covered count/find
        // B/L 필터 필드(jobDiv 등)는 residual(FETCH 후 잔차)이므로 전용 인덱스 불필요
        ops.ensureIndex(perfDtTrailing("hasFreightInput"));
        ops.ensureIndex(perfDtTrailing("hasTaxIssued"));
        ops.ensureIndex(perfDtTrailing("hasSlipIssued"));

        // deleteMany({blKey: ...}) 가속 — 증분/full ETL 시 컬렉션 풀스캔 방지
        ops.ensureIndex(new Index().on("blKey", Sort.Direction.ASC));
    }

    /**
     * pms_perfdt_entry의 flag 기반 인덱스 공통 후행 키(pd, blKey, blId, blType).
     * covered count: {flag, pd} 범위로 distinct pd 집합 조회 경로를 인덱스로 완전 처리.
     */
    private Index perfDtTrailing(String flagField) {
        return new Index()
            .on(flagField, Sort.Direction.ASC)
            .on("pd", Sort.Direction.ASC)
            .on("blKey", Sort.Direction.ASC)
            .on("blId", Sort.Direction.ASC)
            .on("blType", Sort.Direction.ASC);
    }

    // ── pms_docdt_entry ──────────────────────────────────────────────────────

    private void buildDocDtIndexes() {
        IndexOperations ops = mongoTemplate.indexOps(PmsDocDtEntryDocument.class);

        // 날짜 2종 — 날짜로 IXSCAN 좁히고 나머진 FETCH 후 residual 필터
        // docType/status/teamCode 등 필터는 날짜 인덱스 IXSCAN 후 잔차로 처리
        ops.ensureIndex(docDtTrailing("perfPd"));
        ops.ensureIndex(docDtTrailing("docDt"));

        // deleteMany({blKey: ...}) 가속 — 증분/full ETL 시 컬렉션 풀스캔 방지
        ops.ensureIndex(new Index().on("blKey", Sort.Direction.ASC));
    }

    /**
     * 날짜 단일 필드 → trailing(blKey, blId, blType).
     * 날짜 범위 필터만 있는 count/find 경로.
     */
    private Index docDtTrailing(String dateField) {
        return new Index()
            .on(dateField, Sort.Direction.ASC)
            .on("blKey", Sort.Direction.ASC)
            .on("blId", Sort.Direction.ASC)
            .on("blType", Sort.Direction.ASC);
    }
}
