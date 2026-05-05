package com.freightos.fms.adapter.in.web.enums;

import com.freightos.fms.adapter.in.web.enums.dto.EnumMapResponse;
import com.freightos.fms.adapter.in.web.enums.dto.EnumOptionResponse;
import com.freightos.fms.domain.enums.EnumOption;
import com.freightos.fms.domain.enums.port.in.EnumQueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EnumAssemblerTest {

    private EnumAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new EnumAssembler();
    }

    @Test
    @DisplayName("description 보유 EnumOption → EnumOptionResponse 변환 시 모든 필드 정확히 매핑")
    void toResponse_withDescription_mapsAllFields() {
        EnumOption option = new EnumOption("SHP", "Ship", "Ship");

        List<EnumOptionResponse> result = assembler.toResponse(List.of(option));

        assertThat(result).hasSize(1);
        EnumOptionResponse response = result.get(0);
        assertThat(response.code()).isEqualTo("SHP");
        assertThat(response.label()).isEqualTo("Ship");
        assertThat(response.description()).isEqualTo("Ship");
    }

    @Test
    @DisplayName("description null인 EnumOption → EnumOptionResponse의 description도 null")
    void toResponse_withNullDescription_responseDescriptionIsNull() {
        EnumOption option = new EnumOption("EXP", "EXP", null);

        List<EnumOptionResponse> result = assembler.toResponse(List.of(option));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isNull();
    }

    @Test
    @DisplayName("EnumQueryResult → EnumMapResponse 변환 시 found/notFound 모두 정확히 매핑")
    void toMapResponse_mapsFoundAndNotFound() {
        EnumOption boundOption = new EnumOption("EXP", "EXP", null);
        EnumOption perOption = new EnumOption("SHP", "Ship", "Ship");
        EnumQueryResult queryResult = new EnumQueryResult(
                Map.of("Bound", List.of(boundOption), "Per", List.of(perOption)),
                List.of("Unknown"));

        EnumMapResponse response = assembler.toMapResponse(queryResult);

        assertThat(response.enums()).containsKeys("Bound", "Per");
        assertThat(response.enums().get("Bound")).hasSize(1);
        assertThat(response.enums().get("Bound").get(0).code()).isEqualTo("EXP");
        assertThat(response.enums().get("Per")).hasSize(1);
        assertThat(response.enums().get("Per").get(0).label()).isEqualTo("Ship");
        assertThat(response.notFound()).containsExactly("Unknown");
    }

    @Test
    @DisplayName("notFound가 비어있는 EnumQueryResult → EnumMapResponse.notFound 빈 리스트")
    void toMapResponse_emptyNotFound_responseNotFoundIsEmpty() {
        EnumOption option = new EnumOption("A", "A", null);
        EnumQueryResult queryResult = new EnumQueryResult(
                Map.of("BlType", List.of(option)),
                List.of());

        EnumMapResponse response = assembler.toMapResponse(queryResult);

        assertThat(response.notFound()).isEmpty();
    }
}
