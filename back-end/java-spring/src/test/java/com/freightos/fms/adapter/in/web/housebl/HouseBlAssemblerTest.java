package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HouseBlAssemblerTest {

    private final HouseBlAssembler assembler = new HouseBlAssembler();

    // ── toEntity(CreateHouseBlRequest) ────────────────────────────────

    @Test
    @DisplayName("toEntity: jobDiv=SEA, bound=EXP → HouseBlSea 인스턴스 생성, jobDiv·bound 일치")
    void toEntity_seaExp_returnsHouseBlSeaWithCorrectJobDivAndBound() {
        CreateHouseBlRequest request = new CreateHouseBlRequest(JobDiv.SEA, Bound.EXP);

        HouseBl result = assembler.toEntity(request);

        assertThat(result).isInstanceOf(HouseBlSea.class);
        assertThat(result.getJobDiv()).isEqualTo(JobDiv.SEA);
        assertThat(result.getBound()).isEqualTo(Bound.EXP);
    }

    @Test
    @DisplayName("toEntity: jobDiv=AIR, bound=IMP → HouseBlAir 인스턴스 생성, bound 일치")
    void toEntity_airImp_returnsHouseBlAirWithCorrectBound() {
        CreateHouseBlRequest request = new CreateHouseBlRequest(JobDiv.AIR, Bound.IMP);

        HouseBl result = assembler.toEntity(request);

        assertThat(result).isInstanceOf(HouseBlAir.class);
        assertThat(result.getJobDiv()).isEqualTo(JobDiv.AIR);
        assertThat(result.getBound()).isEqualTo(Bound.IMP);
    }

    @Test
    @DisplayName("toEntity: jobDiv=TRUCK → UnsupportedOperationException")
    void toEntity_truckJobDiv_throwsUnsupportedOperationException() {
        CreateHouseBlRequest request = new CreateHouseBlRequest(JobDiv.TRUCK, Bound.EXP);

        assertThatThrownBy(() -> assembler.toEntity(request))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("TRUCK");
    }

    @Test
    @DisplayName("toEntity: jobDiv=NON_BL → UnsupportedOperationException")
    void toEntity_nonBlJobDiv_throwsUnsupportedOperationException() {
        CreateHouseBlRequest request = new CreateHouseBlRequest(JobDiv.NON_BL, Bound.EXP);

        assertThatThrownBy(() -> assembler.toEntity(request))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("toEntity: jobDiv=SEA, bound=IMP → HouseBlSea, bound IMP 확인")
    void toEntity_seaImp_returnsHouseBlSeaWithImpBound() {
        CreateHouseBlRequest request = new CreateHouseBlRequest(JobDiv.SEA, Bound.IMP);

        HouseBl result = assembler.toEntity(request);

        assertThat(result).isInstanceOf(HouseBlSea.class);
        assertThat(result.getBound()).isEqualTo(Bound.IMP);
    }
}
