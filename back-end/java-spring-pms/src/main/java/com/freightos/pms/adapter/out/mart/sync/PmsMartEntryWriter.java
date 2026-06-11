package com.freightos.pms.adapter.out.mart.sync;

import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.adapter.out.mart.document.PmsDocDtEntryDocument;
import com.freightos.pms.adapter.out.mart.document.PmsPerfDtEntryDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * master(pms.mart.enabled)와 하위 플래그 모두 true일 때만 활성 — mart off 시 계열 전체 off
 *
 * 적재 전략: blKey 범위 delete-then-insert(stale 제거).
 * full 병렬 워커는 freight_header_id(=blKey, 1:1) 레인지가 서로소이므로 blKey 충돌 없다.
 * 한 배치 규모는 batchSize(기본 2000) B/L이고 sidecar는 그보다 작으므로 단일 배치 insert 충분.
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = {"enabled", "line-accel.enabled"}, havingValue = "true")
@RequiredArgsConstructor
class PmsMartEntryWriter {

    private final MongoTemplate mongoTemplate;

    /**
     * B/L 문서 배치에서 sidecar 엔트리를 파생해 MongoDB에 적재한다.
     *
     * @param docs ETL에서 방금 upsert된 B/L 문서 배치
     */
    void writeFromDocs(List<PmsBlMartDocument> docs) {
        if (docs.isEmpty()) {
            return;
        }

        List<String> blKeys = docs.stream().map(PmsBlMartDocument::getId).collect(Collectors.toList());

        // pms_perfdt_entry 갱신
        List<PmsPerfDtEntryDocument> perfList = PmsMartEntryDeriver.derivePerfDt(docs);
        mongoTemplate.remove(
            Query.query(Criteria.where("blKey").in(blKeys)),
            PmsPerfDtEntryDocument.class);
        if (!perfList.isEmpty()) {
            mongoTemplate.insert(perfList, PmsPerfDtEntryDocument.class);
        }

        // pms_docdt_entry 갱신
        List<PmsDocDtEntryDocument> docDtList = PmsMartEntryDeriver.deriveDocDt(docs);
        mongoTemplate.remove(
            Query.query(Criteria.where("blKey").in(blKeys)),
            PmsDocDtEntryDocument.class);
        if (!docDtList.isEmpty()) {
            mongoTemplate.insert(docDtList, PmsDocDtEntryDocument.class);
        }
    }
}
