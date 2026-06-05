package com.freightos.fms.adapter.out.persistence.freight;

import com.freightos.fms.application.freight.FreightLineView;
import com.freightos.fms.application.freight.FreightView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FreightJpaToDomainMapper 단위 테스트.
 * DB/Spring 컨텍스트 불필요 — 순수 매핑 로직 검증.
 */
class FreightJpaToDomainMapperTest {

    private final FreightJpaToDomainMapper mapper = new FreightJpaToDomainMapper();

    @Test
    @DisplayName("toFreightView: financial_document_id 보유 라인에 documentNo가 채워진다")
    void toFreightView_issuedLine_financialDocumentNoIsFilled() {
        FreightHeaderJpaEntity header = buildMinimalHeader();
        FreightLineJpaEntity issuedLine = buildLine(header, 100L);
        addLinesToHeader(header, List.of(issuedLine));

        Map<Long, String> documentNoMap = Map.of(100L, "INV-2026-00001");

        FreightView view = mapper.toFreightView(header, Collections.emptyMap(), Collections.emptyMap(), documentNoMap);

        List<FreightLineView> lineViews = view.lines();
        assertThat(lineViews).hasSize(1);
        assertThat(lineViews.get(0).financialDocumentId()).isEqualTo(100L);
        assertThat(lineViews.get(0).financialDocumentNo()).isEqualTo("INV-2026-00001");
    }

    @Test
    @DisplayName("toFreightView: financial_document_id 없는 라인의 financialDocumentNo는 null")
    void toFreightView_unissuedLine_financialDocumentNoIsNull() {
        FreightHeaderJpaEntity header = buildMinimalHeader();
        FreightLineJpaEntity unissuedLine = buildLine(header, null);
        addLinesToHeader(header, List.of(unissuedLine));

        FreightView view = mapper.toFreightView(header, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

        List<FreightLineView> lineViews = view.lines();
        assertThat(lineViews).hasSize(1);
        assertThat(lineViews.get(0).financialDocumentId()).isNull();
        assertThat(lineViews.get(0).financialDocumentNo()).isNull();
    }

    @Test
    @DisplayName("toFreightView: documentNoMap에 id가 없으면 financialDocumentNo는 null")
    void toFreightView_documentIdNotInMap_financialDocumentNoIsNull() {
        FreightHeaderJpaEntity header = buildMinimalHeader();
        FreightLineJpaEntity issuedLine = buildLine(header, 999L);
        addLinesToHeader(header, List.of(issuedLine));

        // 맵에 999L 없음
        FreightView view = mapper.toFreightView(header, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

        assertThat(view.lines().get(0).financialDocumentId()).isEqualTo(999L);
        assertThat(view.lines().get(0).financialDocumentNo()).isNull();
    }

    @Test
    @DisplayName("toFreightView: 발행 라인과 미발행 라인 혼재 시 각각 올바르게 매핑된다")
    void toFreightView_mixedLines_eachMappedCorrectly() {
        FreightHeaderJpaEntity header = buildMinimalHeader();
        FreightLineJpaEntity issuedLine = buildLine(header, 1L);
        FreightLineJpaEntity unissuedLine = buildLine(header, null);
        addLinesToHeader(header, List.of(issuedLine, unissuedLine));

        Map<Long, String> documentNoMap = Map.of(1L, "PAY-2026-00001");

        FreightView view = mapper.toFreightView(header, Collections.emptyMap(), Collections.emptyMap(), documentNoMap);

        assertThat(view.lines()).hasSize(2);
        FreightLineView first = view.lines().get(0);
        FreightLineView second = view.lines().get(1);

        assertThat(first.financialDocumentNo()).isEqualTo("PAY-2026-00001");
        assertThat(second.financialDocumentNo()).isNull();
    }

    // ── 헬퍼 ────────────────────────────────────────────────────────────────────

    private FreightHeaderJpaEntity buildMinimalHeader() {
        FreightHeaderJpaEntity header = new FreightHeaderJpaEntity();
        header.setBlType("HOUSE");
        header.setBlId(1L);
        return header;
    }

    private FreightLineJpaEntity buildLine(FreightHeaderJpaEntity header, Long financialDocumentId) {
        FreightLineJpaEntity line = new FreightLineJpaEntity();
        line.setFreightHeader(header);
        line.setFreightType("SELLING");
        line.setFinancialDocumentId(financialDocumentId);
        return line;
    }

    private void addLinesToHeader(FreightHeaderJpaEntity header, List<FreightLineJpaEntity> lines) {
        header.syncLines(lines);
    }
}
