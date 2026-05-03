package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.CreateMasterBlRequest;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MasterBlAssemblerTest {

    private final MasterBlAssembler assembler = new MasterBlAssembler();

    // ── toEntity(CreateMasterBlRequest) ──────────────────────────────

    @Test
    @DisplayName("toEntity: jobDiv=SEA, bound=EXP → MasterBlSea 인스턴스 생성, bound 일치")
    void toEntity_seaExp_returnsMasterBlSeaWithCorrectBound() {
        CreateMasterBlRequest request = new CreateMasterBlRequest(
                MasterBlJobDiv.SEA, Bound.EXP, null, null,
                FreightTerm.PREPAID,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        MasterBl result = assembler.toEntity(request);

        assertThat(result).isInstanceOf(MasterBlSea.class);
        assertThat(result.getBound()).isEqualTo(Bound.EXP);
    }

    @Test
    @DisplayName("toEntity: jobDiv=AIR, bound=IMP → MasterBlAir 인스턴스 생성, bound 일치")
    void toEntity_airImp_returnsMasterBlAirWithCorrectBound() {
        CreateMasterBlRequest request = new CreateMasterBlRequest(
                MasterBlJobDiv.AIR, Bound.IMP, null, null,
                FreightTerm.PREPAID,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        MasterBl result = assembler.toEntity(request);

        assertThat(result).isInstanceOf(MasterBlAir.class);
        assertThat(result.getBound()).isEqualTo(Bound.IMP);
    }

    @Test
    @DisplayName("toEntity: jobDiv=SEA, bound=IMP → MasterBlSea, bound IMP 확인")
    void toEntity_seaImp_returnsMasterBlSeaWithImpBound() {
        CreateMasterBlRequest request = new CreateMasterBlRequest(
                MasterBlJobDiv.SEA, Bound.IMP, null, null,
                FreightTerm.PREPAID,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        MasterBl result = assembler.toEntity(request);

        assertThat(result).isInstanceOf(MasterBlSea.class);
        assertThat(result.getBound()).isEqualTo(Bound.IMP);
    }

    @Test
    @DisplayName("toEntity: jobDiv=AIR, bound=EXP → MasterBlAir, jobDiv AIR 확인")
    void toEntity_airExp_returnsMasterBlAirWithAirJobDiv() {
        CreateMasterBlRequest request = new CreateMasterBlRequest(
                MasterBlJobDiv.AIR, Bound.EXP, null, null,
                FreightTerm.PREPAID,
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        MasterBl result = assembler.toEntity(request);

        assertThat(result).isInstanceOf(MasterBlAir.class);
        assertThat(result.getJobDiv()).isEqualTo(MasterBlJobDiv.AIR);
    }
}
