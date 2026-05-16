package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.application.masterbl.projection.ConsoledHouseBlSummaryView;
import com.freightos.fms.application.masterbl.projection.DescProjection;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MasterBlDetailResponseTest {

    // ── record 구조 검증 ─────────────────────────────────────────────

    @Test
    @DisplayName("MasterBlDetailResponse: record 컴포넌트가 정확히 35개이고, consolidatedHouseBls 위치 정합")
    void recordComponents_haveExactlyThirtyFiveFields() {
        var components = MasterBlDetailResponse.class.getRecordComponents();

        assertThat(components).hasSize(35);
        assertThat(components[29].getName()).isEqualTo("consolidatedHouseBls");
        assertThat(components[29].getType()).isEqualTo(List.class);
    }

    // ── from(MasterBlDetailResult) 매핑 검증 ─────────────────────────

    @Test
    @DisplayName("from(MasterBlDetailResult): 콘솔 리스트가 응답 마지막 필드까지 매핑된다")
    void from_withMasterBlDetailResultContainingSea_mapsAllFieldsAndConsolidatedList() {
        ConsoledHouseBlSummaryView seaSummary = new ConsoledHouseBlSummaryView(
                1L, "HBL-001", "SHIP01", "CONS01", "DOC01",
                10, "CTN", null, BigDecimal.valueOf(100), BigDecimal.valueOf(1),
                "20251130", "20251201", "VESSEL A", "V001", "KRPUS", "USNYC",
                null
        );
        MasterBlDetailResult result = new MasterBlDetailResult(
                1L, "MBL-001", "REF-001",
                "SEA", "EXP", "HOUSE",
                "SHIP01", "CONS01", "NOTIFY01",
                null, null, null,
                "KRPUS", "USNYC", "20251130", "20251201",
                "PREPAID", "OPR01", "TEAM01",
                10, "KGS", null, BigDecimal.valueOf(100), BigDecimal.valueOf(1),
                "MAIN ITEM", "1234.56", "SETTLE01",
                null, null,
                List.of(seaSummary),
                List.of(),
                "REMARK-SAMPLE",
                new DescProjection("MARKS", "DESC", null, null),
                null,
                null
        );

        MasterBlDetailResponse response = MasterBlDetailResponse.from(result);

        assertThat(response).isNotNull();
        assertThat(response.consolidatedHouseBls()).hasSize(1);
        assertThat(response.consolidatedHouseBls().get(0)).isEqualTo(seaSummary);
        assertThat(response.mainItemName()).isEqualTo("MAIN ITEM");
        assertThat(response.hsCode()).isEqualTo("1234.56");
        assertThat(response.settlePartnerCode()).isEqualTo("SETTLE01");
        assertThat(response.desc()).isNotNull();
        assertThat(response.desc().marks()).isEqualTo("MARKS");
    }

    @Test
    @DisplayName("from(MasterBlDetailResult): 빈 consolidated 리스트도 그대로 보존(null 변환 안 함)")
    void from_withEmptyConsolidatedList_returnsResponseWithEmptyList() {
        MasterBlDetailResult result = new MasterBlDetailResult(
                2L, "MBL-002", null,
                "SEA", "EXP", "HOUSE",
                null, null, null,
                null, null, null,
                null, null, null, null,
                "PREPAID", null, null,
                null, null, null, null, null,
                null, null, null,
                null, null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null
        );

        MasterBlDetailResponse response = MasterBlDetailResponse.from(result);

        assertThat(response.consolidatedHouseBls()).isNotNull().isEmpty();
    }
}
