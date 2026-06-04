package com.freightos.fms.adapter.out.persistence.freight;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FreightHeaderJpaEntity.syncUnissuedLines 단위 테스트.
 * DB/Spring 컨텍스트 불필요 — 순수 Java 로직 검증.
 */
class FreightHeaderJpaEntityTest {

    /**
     * 발행 라인(financial_document_id != null)이 syncUnissuedLines 후에도 보존된다.
     */
    @Test
    @DisplayName("syncUnissuedLines: 발행 라인은 보존되고 미발행 라인만 교체된다")
    void syncUnissuedLines_issuedLineIsPreserved() {
        FreightHeaderJpaEntity header = new FreightHeaderJpaEntity();

        // 발행 라인 (financial_document_id 설정)
        FreightLineJpaEntity issuedLine = new FreightLineJpaEntity();
        issuedLine.setFreightHeader(header);
        issuedLine.setFreightType("SELLING");
        issuedLine.setFinancialDocumentId(100L);

        // 미발행 라인
        FreightLineJpaEntity unissuedLine = new FreightLineJpaEntity();
        unissuedLine.setFreightHeader(header);
        unissuedLine.setFreightType("BUYING");

        header.syncLines(List.of(issuedLine, unissuedLine));

        // 새 미발행 라인으로 교체
        FreightLineJpaEntity newLine = new FreightLineJpaEntity();
        newLine.setFreightHeader(header);
        newLine.setFreightType("SELLING");

        header.syncUnissuedLines(List.of(newLine));

        assertThat(header.getLines()).hasSize(2);
        assertThat(header.getLines()).contains(issuedLine);
        assertThat(header.getLines()).contains(newLine);
        assertThat(header.getLines()).doesNotContain(unissuedLine);
        // 발행 라인의 financial_document_id 불변 보장
        assertThat(issuedLine.getFinancialDocumentId()).isEqualTo(100L);
    }

    /**
     * 발행 라인이 없는 상태에서 syncUnissuedLines 호출 시 전량 교체된다.
     */
    @Test
    @DisplayName("syncUnissuedLines: 발행 라인이 없으면 미발행 라인 전량 교체")
    void syncUnissuedLines_noIssuedLines_replacesAll() {
        FreightHeaderJpaEntity header = new FreightHeaderJpaEntity();

        FreightLineJpaEntity oldLine = new FreightLineJpaEntity();
        oldLine.setFreightHeader(header);
        oldLine.setFreightType("SELLING");
        header.syncLines(List.of(oldLine));

        FreightLineJpaEntity newLine = new FreightLineJpaEntity();
        newLine.setFreightHeader(header);
        newLine.setFreightType("BUYING");

        header.syncUnissuedLines(List.of(newLine));

        assertThat(header.getLines()).containsExactly(newLine);
        assertThat(header.getLines()).doesNotContain(oldLine);
    }

    /**
     * 빈 newLines로 syncUnissuedLines 호출 시 발행 라인만 남고 미발행 전부 제거된다.
     */
    @Test
    @DisplayName("syncUnissuedLines: 빈 newLines 입력 시 발행 라인만 유지")
    void syncUnissuedLines_emptyNewLines_keepsOnlyIssuedLines() {
        FreightHeaderJpaEntity header = new FreightHeaderJpaEntity();

        FreightLineJpaEntity issuedLine = new FreightLineJpaEntity();
        issuedLine.setFreightHeader(header);
        issuedLine.setFinancialDocumentId(200L);

        FreightLineJpaEntity unissuedLine = new FreightLineJpaEntity();
        unissuedLine.setFreightHeader(header);

        header.syncLines(List.of(issuedLine, unissuedLine));

        header.syncUnissuedLines(List.of());

        assertThat(header.getLines()).containsExactly(issuedLine);
    }

    /**
     * 모든 라인이 발행된 상태에서 syncUnissuedLines 호출 시 발행 라인 전부 보존된다.
     */
    @Test
    @DisplayName("syncUnissuedLines: 모든 라인이 발행 상태면 기존 컬렉션에 newLines만 추가")
    void syncUnissuedLines_allIssuedLines_addsNewLinesAdditionally() {
        FreightHeaderJpaEntity header = new FreightHeaderJpaEntity();

        FreightLineJpaEntity issued1 = new FreightLineJpaEntity();
        issued1.setFreightHeader(header);
        issued1.setFinancialDocumentId(1L);

        FreightLineJpaEntity issued2 = new FreightLineJpaEntity();
        issued2.setFreightHeader(header);
        issued2.setFinancialDocumentId(2L);

        header.syncLines(List.of(issued1, issued2));

        FreightLineJpaEntity newLine = new FreightLineJpaEntity();
        newLine.setFreightHeader(header);

        header.syncUnissuedLines(List.of(newLine));

        assertThat(header.getLines()).hasSize(3);
        assertThat(header.getLines()).contains(issued1, issued2, newLine);
    }

    /**
     * 리스트 참조 동일성 — syncUnissuedLines 후에도 기존 lines 컬렉션 인스턴스가 유지된다.
     * orphanRemoval이 동작하려면 같은 리스트 인스턴스를 변경해야 한다.
     */
    @Test
    @DisplayName("syncUnissuedLines: lines 컬렉션 참조 동일성 유지 (orphanRemoval 지원)")
    void syncUnissuedLines_preservesListIdentity() {
        FreightHeaderJpaEntity header = new FreightHeaderJpaEntity();
        List<FreightLineJpaEntity> originalRef = header.getLines();

        header.syncUnissuedLines(List.of());

        assertThat(header.getLines()).isSameAs(originalRef);
    }
}
