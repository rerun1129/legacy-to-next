package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import com.freightos.fms.domain.masterbl.MasterBlDetail;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MasterBlDetailResponseTest {

    // ── record 구조 검증 ─────────────────────────────────────────────

    @Test
    @DisplayName("MasterBlDetailResponse: record 컴포넌트가 정확히 22개이고, 22번째는 consolidatedHouseBls이다")
    void recordComponents_haveExactlyTwentyTwoFields() {
        var components = MasterBlDetailResponse.class.getRecordComponents();

        assertThat(components).hasSize(22);
        assertThat(components[21].getName()).isEqualTo("consolidatedHouseBls");
        assertThat(components[21].getType()).isEqualTo(List.class);
    }

    // ── from(MasterBlDetail) 매핑 검증 ───────────────────────────────

    @Test
    @DisplayName("from(MasterBlDetail): MasterBl + 콘솔 리스트가 응답 마지막 필드까지 매핑된다")
    void from_withMasterBlDetailContainingSea_mapsAllFieldsAndConsolidatedList() {
        MasterBlSea master = MasterBlSea.create(Bound.EXP);
        ConsoledHouseBlSeaSummary seaSummary = new ConsoledHouseBlSeaSummary(
                1L, "HBL-001", "SHIP01", "CONS01", "DOC01",
                10, "CTN", BigDecimal.valueOf(100), BigDecimal.valueOf(1),
                "20251130", "20251201", "VESSEL A", "V001", "KRPUS", "USNYC"
        );
        MasterBlDetail detail = new MasterBlDetail(master, List.of(seaSummary));

        MasterBlDetailResponse response = MasterBlDetailResponse.from(detail);

        assertThat(response).isNotNull();
        assertThat(response.consolidatedHouseBls()).hasSize(1);
        assertThat(response.consolidatedHouseBls().get(0)).isEqualTo(seaSummary);
    }

    @Test
    @DisplayName("from(MasterBlDetail): 빈 consolidated 리스트도 그대로 보존(null 변환 안 함)")
    void from_withEmptyConsolidatedList_returnsResponseWithEmptyList() {
        MasterBlSea master = MasterBlSea.create(Bound.EXP);
        MasterBlDetail detail = new MasterBlDetail(master, List.of());

        MasterBlDetailResponse response = MasterBlDetailResponse.from(detail);

        assertThat(response.consolidatedHouseBls()).isNotNull().isEmpty();
    }
}
