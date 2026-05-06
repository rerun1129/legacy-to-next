package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HouseBlAssemblerTest {

    private final HouseBlAssembler assembler = new HouseBlAssembler();

    // ── toCreateCommand(CreateHouseBlRequest) ─────────────────────────

    @Test
    @DisplayName("toCreateCommand: jobDiv=SEA, bound=EXP → Command의 jobDiv·bound 일치")
    void toCreateCommand_seaExp_returnsCommandWithCorrectJobDivAndBound() {
        CreateHouseBlRequest request = new CreateHouseBlRequest(
                "SEA", "EXP", null,
                "HOUSE", "PREPAID",
                null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );

        CreateHouseBlCommand result = assembler.toCreateCommand(request);

        assertThat(result).isNotNull();
        assertThat(result.jobDiv()).isEqualTo("SEA");
        assertThat(result.bound()).isEqualTo("EXP");
    }

    @Test
    @DisplayName("toCreateCommand: jobDiv=AIR, bound=IMP → Command의 jobDiv·bound 일치")
    void toCreateCommand_airImp_returnsCommandWithCorrectBound() {
        CreateHouseBlRequest request = new CreateHouseBlRequest(
                "AIR", "IMP", null,
                "HOUSE", "PREPAID",
                null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );

        CreateHouseBlCommand result = assembler.toCreateCommand(request);

        assertThat(result).isNotNull();
        assertThat(result.jobDiv()).isEqualTo("AIR");
        assertThat(result.bound()).isEqualTo("IMP");
    }

    @Test
    @DisplayName("toCreateCommand: jobDiv=TRUCK → Command의 jobDiv 일치")
    void toCreateCommand_truckJobDiv_returnsCommandWithCorrectJobDiv() {
        CreateHouseBlRequest request = new CreateHouseBlRequest(
                "TRUCK", "EXP", null,
                "HOUSE", "PREPAID",
                null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );

        CreateHouseBlCommand result = assembler.toCreateCommand(request);

        assertThat(result).isNotNull();
        assertThat(result.jobDiv()).isEqualTo("TRUCK");
    }

    @Test
    @DisplayName("toCreateCommand: jobDiv=NON_BL → Command의 jobDiv 일치")
    void toCreateCommand_nonBlJobDiv_returnsCommandWithCorrectJobDiv() {
        CreateHouseBlRequest request = new CreateHouseBlRequest(
                "NON_BL", "EXP", null,
                "HOUSE", "PREPAID",
                null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );

        CreateHouseBlCommand result = assembler.toCreateCommand(request);

        assertThat(result).isNotNull();
        assertThat(result.jobDiv()).isEqualTo("NON_BL");
    }

    @Test
    @DisplayName("toCreateCommand: jobDiv=SEA, bound=IMP → Command의 bound IMP 확인")
    void toCreateCommand_seaImp_returnsCommandWithImpBound() {
        CreateHouseBlRequest request = new CreateHouseBlRequest(
                "SEA", "IMP", null,
                "HOUSE", "PREPAID",
                null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );

        CreateHouseBlCommand result = assembler.toCreateCommand(request);

        assertThat(result.bound()).isEqualTo("IMP");
    }
}
