package com.freightos.pms.adapter.out.mart.sync;

import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mart 문서를 MongoDB에 _id 기준 멱등 upsert하는 라이터.
 * BulkOperations.UNORDERED로 배치 upsert를 수행하여 왕복을 최소화한다.
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class PmsMartUpsertWriter {

    private final MongoTemplate mongoTemplate;

    /**
     * 배치를 _id 기준 upsert한다.
     * replaceOne(upsert) 방식으로 멱등성을 보장한다.
     *
     * @return upsert된 문서 수
     */
    long upsertBatch(List<PmsBlMartDocument> docs) {
        if (docs.isEmpty()) {
            return 0L;
        }
        BulkOperations bulkOps = mongoTemplate.bulkOps(
            BulkOperations.BulkMode.UNORDERED, PmsBlMartDocument.class);

        for (PmsBlMartDocument doc : docs) {
            Query idQuery = Query.query(Criteria.where("_id").is(doc.getId()));
            bulkOps.replaceOne(idQuery, doc, FindAndReplaceOptions.options().upsert());
        }

        bulkOps.execute();
        return docs.size();
    }
}
