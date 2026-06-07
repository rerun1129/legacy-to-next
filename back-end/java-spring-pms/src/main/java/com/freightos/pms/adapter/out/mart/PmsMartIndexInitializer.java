package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 애플리케이션 기동 시 pms_bl_mart 컬렉션 인덱스를 멱등하게 보장한다.
 * ensureIndex는 동일 정의 재호출 시 기존 인덱스를 그대로 유지하므로 안전하다.
 * pms.mart.enabled=true 일 때만 등록된다.
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsMartIndexInitializer implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        IndexOperations indexOps = mongoTemplate.indexOps(PmsBlMartDocument.class);

        // 기존 2-field basis 인덱스 제거 — 3-field 정렬커버 인덱스로 대체되므로 중복 write 비용 제거
        dropIndexIfExists(indexOps, "hasFreightInput_1_blId_-1");
        dropIndexIfExists(indexOps, "hasTaxIssued_1_blId_-1");
        dropIndexIfExists(indexOps, "hasSlipIssued_1_blId_-1");
        dropIndexIfExists(indexOps, "hasDocumentCreated_1_blId_-1");

        // basis 존재 플래그별 3종 인덱스 — 각 basis 조회의 find/count 경로를 전부 커버
        ensureBasisIndexes(indexOps, "hasFreightInput");
        ensureBasisIndexes(indexOps, "hasTaxIssued");
        ensureBasisIndexes(indexOps, "hasSlipIssued");
        ensureBasisIndexes(indexOps, "hasDocumentCreated");

        // 식별자 복합 인덱스 (각각 + blId DESC 정렬 보조)
        indexOps.ensureIndex(new Index().on("actualCustomerCode", Sort.Direction.ASC).on("blId", Sort.Direction.DESC));
        indexOps.ensureIndex(new Index().on("settlePartnerCode", Sort.Direction.ASC).on("blId", Sort.Direction.DESC));
        indexOps.ensureIndex(new Index().on("linerCode", Sort.Direction.ASC).on("blId", Sort.Direction.DESC));
        indexOps.ensureIndex(new Index().on("salesManCode", Sort.Direction.ASC).on("blId", Sort.Direction.DESC));
        indexOps.ensureIndex(new Index().on("polCode", Sort.Direction.ASC).on("blId", Sort.Direction.DESC));
        indexOps.ensureIndex(new Index().on("podCode", Sort.Direction.ASC).on("blId", Sort.Direction.DESC));
        indexOps.ensureIndex(new Index().on("houseTeamCode", Sort.Direction.ASC).on("blId", Sort.Direction.DESC));
        indexOps.ensureIndex(new Index().on("documentCreated.teamCode", Sort.Direction.ASC).on("blId", Sort.Direction.DESC));

        // B/L 번호 단일 인덱스 (번호 직접 조회 지원)
        indexOps.ensureIndex(new Index().on("houseBlNo", Sort.Direction.ASC));
        indexOps.ensureIndex(new Index().on("masterBlNo", Sort.Direction.ASC));

        // ETD/ETA 범위 필터(실적 화면 1순위) — 인덱스 누락 시 풀스캔 7s → 추가 시 sub-second
        indexOps.ensureIndex(new Index().on("etd", Sort.Direction.ASC));
        indexOps.ensureIndex(new Index().on("eta", Sort.Direction.ASC));
    }

    /**
     * 단일 basis 플래그 필드에 대해 find/count 경로를 커버하는 인덱스 3종을 보장한다.
     * <ul>
     *   <li>정렬커버: {flagField:1, blId:-1, blType:1} — find 페이지 쿼리가 (blId DESC, blType ASC) 정렬을
     *       인덱스로 완전 커버해 limit 조기종료(블로킹 정렬 5.4s 제거)</li>
     *   <li>etd 날짜: {flagField:1, etd:1} — ETD 기간 필터 count의 COUNT_SCAN + 좁은 범위 find 지원</li>
     *   <li>eta 날짜: {flagField:1, eta:1} — ETA 기간 필터 동일 목적</li>
     * </ul>
     */
    private void ensureBasisIndexes(IndexOperations indexOps, String flagField) {
        // 정렬커버 인덱스: blType 포함으로 limit 조기종료 보장
        indexOps.ensureIndex(new Index()
                .on(flagField, Sort.Direction.ASC)
                .on("blId", Sort.Direction.DESC)
                .on("blType", Sort.Direction.ASC));
        // ETD 날짜 필터 count/범위 지원
        indexOps.ensureIndex(new Index()
                .on(flagField, Sort.Direction.ASC)
                .on("etd", Sort.Direction.ASC));
        // ETA 날짜 필터 count/범위 지원
        indexOps.ensureIndex(new Index()
                .on(flagField, Sort.Direction.ASC)
                .on("eta", Sort.Direction.ASC));
    }

    /**
     * 인덱스가 존재할 때만 drop 한다. fresh DB 환경에서도 예외 없이 안전하게 동작한다.
     */
    private void dropIndexIfExists(IndexOperations indexOps, String indexName) {
        Set<String> existingNames = indexOps.getIndexInfo().stream()
                .map(info -> info.getName())
                .collect(Collectors.toSet());
        if (existingNames.contains(indexName)) {
            indexOps.dropIndex(indexName);
        }
    }
}
