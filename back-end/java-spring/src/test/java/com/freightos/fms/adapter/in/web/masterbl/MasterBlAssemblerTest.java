package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.CreateMasterBlRequest;
import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MasterBlAssemblerTest {

    private final MasterBlAssembler assembler = new MasterBlAssembler();

    // ── toCreateCommand(CreateMasterBlRequest) ────────────────────────

    @Test
    @DisplayName("toCreateCommand: jobDiv=SEA, bound=EXP → Command의 jobDiv·bound 일치")
    void toCreateCommand_seaExp_returnsCommandWithCorrectJobDivAndBound() {
        CreateMasterBlRequest request = new CreateMasterBlRequest(
                "SEA", "EXP", null, null,
                "PREPAID",
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null
        );

        CreateMasterBlCommand result = assembler.toCreateCommand(request);

        assertThat(result).isNotNull();
        assertThat(result.jobDiv()).isEqualTo("SEA");
        assertThat(result.bound()).isEqualTo("EXP");
    }

    @Test
    @DisplayName("toCreateCommand: jobDiv=AIR, bound=IMP → Command의 jobDiv·bound 일치")
    void toCreateCommand_airImp_returnsCommandWithCorrectJobDivAndBound() {
        CreateMasterBlRequest request = new CreateMasterBlRequest(
                "AIR", "IMP", null, null,
                "PREPAID",
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null
        );

        CreateMasterBlCommand result = assembler.toCreateCommand(request);

        assertThat(result).isNotNull();
        assertThat(result.jobDiv()).isEqualTo("AIR");
        assertThat(result.bound()).isEqualTo("IMP");
    }

    @Test
    @DisplayName("toCreateCommand: jobDiv=SEA, bound=IMP → Command의 bound IMP 확인")
    void toCreateCommand_seaImp_returnsCommandWithImpBound() {
        CreateMasterBlRequest request = new CreateMasterBlRequest(
                "SEA", "IMP", null, null,
                "PREPAID",
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null
        );

        CreateMasterBlCommand result = assembler.toCreateCommand(request);

        assertThat(result.jobDiv()).isEqualTo("SEA");
        assertThat(result.bound()).isEqualTo("IMP");
    }

    @Test
    @DisplayName("toCreateCommand: jobDiv=AIR, bound=EXP → Command의 jobDiv AIR 확인")
    void toCreateCommand_airExp_returnsCommandWithAirJobDiv() {
        CreateMasterBlRequest request = new CreateMasterBlRequest(
                "AIR", "EXP", null, null,
                "PREPAID",
                null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null
        );

        CreateMasterBlCommand result = assembler.toCreateCommand(request);

        assertThat(result.jobDiv()).isEqualTo("AIR");
    }
}
