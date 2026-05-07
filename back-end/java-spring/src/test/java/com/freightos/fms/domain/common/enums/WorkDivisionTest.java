package com.freightos.fms.domain.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkDivision enum 단위 테스트")
class WorkDivisionTest {

    @Test
    @DisplayName("values() 길이가 4이다")
    void values_hasFourEntries() {
        assertThat(WorkDivision.values()).hasSize(4);
    }

    @Test
    @DisplayName("SEA — name()=SEA, getLabel()=Sea")
    void sea_nameAndLabel() {
        assertThat(WorkDivision.SEA.name()).isEqualTo("SEA");
        assertThat(WorkDivision.SEA.getLabel()).isEqualTo("Sea");
    }

    @Test
    @DisplayName("AIR — name()=AIR, getLabel()=Air")
    void air_nameAndLabel() {
        assertThat(WorkDivision.AIR.name()).isEqualTo("AIR");
        assertThat(WorkDivision.AIR.getLabel()).isEqualTo("Air");
    }

    @Test
    @DisplayName("WAREHOUSE — name()=WAREHOUSE, getLabel()=Warehouse")
    void warehouse_nameAndLabel() {
        assertThat(WorkDivision.WAREHOUSE.name()).isEqualTo("WAREHOUSE");
        assertThat(WorkDivision.WAREHOUSE.getLabel()).isEqualTo("Warehouse");
    }

    @Test
    @DisplayName("TRUCKING — name()=TRUCKING, getLabel()=Trucking")
    void trucking_nameAndLabel() {
        assertThat(WorkDivision.TRUCKING.name()).isEqualTo("TRUCKING");
        assertThat(WorkDivision.TRUCKING.getLabel()).isEqualTo("Trucking");
    }
}
