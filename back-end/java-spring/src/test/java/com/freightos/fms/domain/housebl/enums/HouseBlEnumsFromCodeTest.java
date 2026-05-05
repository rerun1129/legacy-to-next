package com.freightos.fms.domain.housebl.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HouseBl Enum fromCode() 단위 테스트")
class HouseBlEnumsFromCodeTest {

    // ────────────────────────────────────────────────
    // ContainerType
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("ContainerType")
    class ContainerTypeTests {

        @Test
        @DisplayName("유효한 코드 '20GP' → ContainerType.T20GP 반환")
        void fromCode_valid20GP_returnsT20GP() {
            assertThat(ContainerType.fromCode("20GP")).isEqualTo(ContainerType.T20GP);
        }

        @Test
        @DisplayName("유효한 코드 '40HQ' → ContainerType.F40HQ 반환")
        void fromCode_valid40HQ_returnsF40HQ() {
            assertThat(ContainerType.fromCode("40HQ")).isEqualTo(ContainerType.F40HQ);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException, 메시지에 코드 포함")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> ContainerType.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown container type code: UNKNOWN_CODE");
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(ContainerType.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(ContainerType.fromCode("")).isNull();
        }

        @Test
        @DisplayName("공백 문자열 전달 → null 반환")
        void fromCode_whitespaceOnly_returnsNull() {
            assertThat(ContainerType.fromCode("   ")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // TruckType
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("TruckType")
    class TruckTypeTests {

        @Test
        @DisplayName("유효한 코드 'T12' → TruckType.T12 반환")
        void fromCode_validT12_returnsT12() {
            assertThat(TruckType.fromLabel("T12")).isEqualTo(TruckType.T12);
        }

        @Test
        @DisplayName("유효한 코드 'T100' → TruckType.T100 반환")
        void fromCode_validT100_returnsT100() {
            assertThat(TruckType.fromLabel("T100")).isEqualTo(TruckType.T100);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException, 메시지에 코드 포함")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> TruckType.fromLabel("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown truck type code: UNKNOWN_CODE");
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(TruckType.fromLabel(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(TruckType.fromLabel("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // HandlingInfoCode
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("HandlingInfoCode")
    class HandlingInfoCodeTests {

        @Test
        @DisplayName("유효한 코드 'A' → HandlingInfoCode.A 반환")
        void fromCode_validA_returnsA() {
            assertThat(HandlingInfoCode.fromCode("A")).isEqualTo(HandlingInfoCode.A);
        }

        @Test
        @DisplayName("유효한 코드 'B' → HandlingInfoCode.B 반환")
        void fromCode_validB_returnsB() {
            assertThat(HandlingInfoCode.fromCode("B")).isEqualTo(HandlingInfoCode.B);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException, 메시지에 코드 포함")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> HandlingInfoCode.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown HandlingInfoCode: UNKNOWN_CODE");
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(HandlingInfoCode.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(HandlingInfoCode.fromCode("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // CargoType
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("CargoType")
    class CargoTypeTests {

        @Test
        @DisplayName("유효한 코드 'NR' → CargoType.NR 반환")
        void fromCode_validNR_returnsNR() {
            assertThat(CargoType.fromCode("NR")).isEqualTo(CargoType.NR);
        }

        @Test
        @DisplayName("유효한 코드 'DG' → CargoType.DG 반환")
        void fromCode_validDG_returnsDG() {
            assertThat(CargoType.fromCode("DG")).isEqualTo(CargoType.DG);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException, 메시지에 코드 포함")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> CargoType.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown CargoType code: UNKNOWN_CODE");
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(CargoType.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(CargoType.fromCode("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // NoOfBl — fromNumber(Integer) 사용
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("NoOfBl")
    class NoOfBlTests {

        @Test
        @DisplayName("유효한 번호 0 → NoOfBl.ZERO 반환")
        void fromNumber_validZero_returnsZero() {
            assertThat(NoOfBl.fromNumber(0)).isEqualTo(NoOfBl.ZERO);
        }

        @Test
        @DisplayName("유효한 번호 3 → NoOfBl.THREE 반환")
        void fromNumber_validThree_returnsThree() {
            assertThat(NoOfBl.fromNumber(3)).isEqualTo(NoOfBl.THREE);
        }

        @Test
        @DisplayName("존재하지 않는 번호 → IllegalArgumentException, 메시지에 번호 포함")
        void fromNumber_unknownNumber_throwsIae() {
            assertThatThrownBy(() -> NoOfBl.fromNumber(99))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown NoOfBl number: 99");
        }

        @Test
        @DisplayName("음수 번호 → IllegalArgumentException")
        void fromNumber_negativeNumber_throwsIae() {
            assertThatThrownBy(() -> NoOfBl.fromNumber(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromNumber_null_returnsNull() {
            assertThat(NoOfBl.fromNumber(null)).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // Fhd — valueOf() 위임
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("Fhd")
    class FhdTests {

        @Test
        @DisplayName("유효한 코드 'N' → Fhd.N 반환")
        void fromCode_validN_returnsN() {
            assertThat(Fhd.fromCode("N")).isEqualTo(Fhd.N);
        }

        @Test
        @DisplayName("유효한 코드 'F' → Fhd.F 반환")
        void fromCode_validF_returnsF() {
            assertThat(Fhd.fromCode("F")).isEqualTo(Fhd.F);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> Fhd.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(Fhd.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(Fhd.fromCode("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // SalesClass — valueOf() 위임
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("SalesClass")
    class SalesClassTests {

        @Test
        @DisplayName("유효한 코드 'S' → SalesClass.S 반환")
        void fromCode_validS_returnsS() {
            assertThat(SalesClass.fromCode("S")).isEqualTo(SalesClass.S);
        }

        @Test
        @DisplayName("유효한 코드 'N' → SalesClass.N 반환")
        void fromCode_validN_returnsN() {
            assertThat(SalesClass.fromCode("N")).isEqualTo(SalesClass.N);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> SalesClass.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(SalesClass.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(SalesClass.fromCode("")).isNull();
        }
    }
}
