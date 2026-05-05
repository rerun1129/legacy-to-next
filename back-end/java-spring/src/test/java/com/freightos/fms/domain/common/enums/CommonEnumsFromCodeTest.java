package com.freightos.fms.domain.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Common Enum fromCode() 단위 테스트")
class CommonEnumsFromCodeTest {

    // ────────────────────────────────────────────────
    // Per
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("Per")
    class PerTests {

        @Test
        @DisplayName("유효한 코드 'SHP' → Per.SHP 반환")
        void fromCode_validSHP_returnsSHP() {
            assertThat(Per.fromCode("SHP")).isEqualTo(Per.SHP);
        }

        @Test
        @DisplayName("유효한 코드 'BL' → Per.BL 반환")
        void fromCode_validBL_returnsBL() {
            assertThat(Per.fromCode("BL")).isEqualTo(Per.BL);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException, 메시지에 코드 포함")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> Per.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown Per code: UNKNOWN_CODE");
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(Per.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(Per.fromCode("")).isNull();
        }

        @Test
        @DisplayName("공백 문자열 전달 → null 반환")
        void fromCode_whitespaceOnly_returnsNull() {
            assertThat(Per.fromCode("   ")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // ServiceTerm
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("ServiceTerm")
    class ServiceTermTests {

        @Test
        @DisplayName("유효한 코드 'CY/CY' → ServiceTerm.CY_CY 반환")
        void fromCode_validCyCy_returnsCyCy() {
            assertThat(ServiceTerm.fromLabel("CY/CY")).isEqualTo(ServiceTerm.CY_CY);
        }

        @Test
        @DisplayName("유효한 코드 'BULK' → ServiceTerm.BULK 반환")
        void fromCode_validBulk_returnsBulk() {
            assertThat(ServiceTerm.fromLabel("BULK")).isEqualTo(ServiceTerm.BULK);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException, 메시지에 코드 포함")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> ServiceTerm.fromLabel("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown service term code: UNKNOWN_CODE");
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(ServiceTerm.fromLabel(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(ServiceTerm.fromLabel("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // FreightTerm — valueOf() 위임, IAE 메시지는 JVM 표준
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("FreightTerm")
    class FreightTermTests {

        @Test
        @DisplayName("유효한 코드 'PREPAID' → FreightTerm.PREPAID 반환")
        void fromCode_validPrepaid_returnsPrepaid() {
            assertThat(FreightTerm.fromCode("PREPAID")).isEqualTo(FreightTerm.PREPAID);
        }

        @Test
        @DisplayName("유효한 코드 'COLLECT' → FreightTerm.COLLECT 반환")
        void fromCode_validCollect_returnsCollect() {
            assertThat(FreightTerm.fromCode("COLLECT")).isEqualTo(FreightTerm.COLLECT);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> FreightTerm.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(FreightTerm.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(FreightTerm.fromCode("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // Incoterms — valueOf() 위임
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("Incoterms")
    class IncotermsTests {

        @Test
        @DisplayName("유효한 코드 'FOB' → Incoterms.FOB 반환")
        void fromCode_validFob_returnsFob() {
            assertThat(Incoterms.fromCode("FOB")).isEqualTo(Incoterms.FOB);
        }

        @Test
        @DisplayName("유효한 코드 'CIF' → Incoterms.CIF 반환")
        void fromCode_validCif_returnsCif() {
            assertThat(Incoterms.fromCode("CIF")).isEqualTo(Incoterms.CIF);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> Incoterms.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(Incoterms.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(Incoterms.fromCode("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // RateClass — valueOf() 위임
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("RateClass")
    class RateClassTests {

        @Test
        @DisplayName("유효한 코드 'M' → RateClass.M 반환")
        void fromCode_validM_returnsM() {
            assertThat(RateClass.fromCode("M")).isEqualTo(RateClass.M);
        }

        @Test
        @DisplayName("유효한 코드 'N' → RateClass.N 반환")
        void fromCode_validN_returnsN() {
            assertThat(RateClass.fromCode("N")).isEqualTo(RateClass.N);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> RateClass.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(RateClass.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(RateClass.fromCode("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // FlightType — valueOf() 위임
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("FlightType")
    class FlightTypeTests {

        @Test
        @DisplayName("유효한 코드 'P' → FlightType.P 반환")
        void fromCode_validP_returnsP() {
            assertThat(FlightType.fromCode("P")).isEqualTo(FlightType.P);
        }

        @Test
        @DisplayName("유효한 코드 'C' → FlightType.C 반환")
        void fromCode_validC_returnsC() {
            assertThat(FlightType.fromCode("C")).isEqualTo(FlightType.C);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> FlightType.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(FlightType.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(FlightType.fromCode("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // FreightCondition — valueOf() 위임
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("FreightCondition")
    class FreightConditionTests {

        @Test
        @DisplayName("유효한 코드 'P' → FreightCondition.P 반환")
        void fromCode_validP_returnsP() {
            assertThat(FreightCondition.fromCode("P")).isEqualTo(FreightCondition.P);
        }

        @Test
        @DisplayName("유효한 코드 'C' → FreightCondition.C 반환")
        void fromCode_validC_returnsC() {
            assertThat(FreightCondition.fromCode("C")).isEqualTo(FreightCondition.C);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> FreightCondition.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(FreightCondition.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(FreightCondition.fromCode("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // SecurityStatus — valueOf() 위임
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("SecurityStatus")
    class SecurityStatusTests {

        @Test
        @DisplayName("유효한 코드 'SPX' → SecurityStatus.SPX 반환")
        void fromCode_validSpx_returnsSpx() {
            assertThat(SecurityStatus.fromCode("SPX")).isEqualTo(SecurityStatus.SPX);
        }

        @Test
        @DisplayName("유효한 코드 'UNK' → SecurityStatus.UNK 반환")
        void fromCode_validUnk_returnsUnk() {
            assertThat(SecurityStatus.fromCode("UNK")).isEqualTo(SecurityStatus.UNK);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> SecurityStatus.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(SecurityStatus.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(SecurityStatus.fromCode("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // WeightUnit — valueOf() 위임
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("WeightUnit")
    class WeightUnitTests {

        @Test
        @DisplayName("유효한 코드 'KGS' → WeightUnit.KGS 반환")
        void fromCode_validKgs_returnsKgs() {
            assertThat(WeightUnit.fromCode("KGS")).isEqualTo(WeightUnit.KGS);
        }

        @Test
        @DisplayName("유효한 코드 'LBS' → WeightUnit.LBS 반환")
        void fromCode_validLbs_returnsLbs() {
            assertThat(WeightUnit.fromCode("LBS")).isEqualTo(WeightUnit.LBS);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> WeightUnit.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(WeightUnit.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(WeightUnit.fromCode("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // DescClause1 — valueOf() 위임
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("DescClause1")
    class DescClause1Tests {

        @Test
        @DisplayName("유효한 코드 'A' → DescClause1.A 반환")
        void fromCode_validA_returnsA() {
            assertThat(DescClause1.fromCode("A")).isEqualTo(DescClause1.A);
        }

        @Test
        @DisplayName("유효한 코드 'G' → DescClause1.G 반환")
        void fromCode_validG_returnsG() {
            assertThat(DescClause1.fromCode("G")).isEqualTo(DescClause1.G);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> DescClause1.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(DescClause1.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(DescClause1.fromCode("")).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // DescClause2 — valueOf() 위임
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("DescClause2")
    class DescClause2Tests {

        @Test
        @DisplayName("유효한 코드 'A' → DescClause2.A 반환")
        void fromCode_validA_returnsA() {
            assertThat(DescClause2.fromCode("A")).isEqualTo(DescClause2.A);
        }

        @Test
        @DisplayName("유효한 코드 'C' → DescClause2.C 반환")
        void fromCode_validC_returnsC() {
            assertThat(DescClause2.fromCode("C")).isEqualTo(DescClause2.C);
        }

        @Test
        @DisplayName("존재하지 않는 코드 → IllegalArgumentException")
        void fromCode_unknownCode_throwsIae() {
            assertThatThrownBy(() -> DescClause2.fromCode("UNKNOWN_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 전달 → null 반환")
        void fromCode_null_returnsNull() {
            assertThat(DescClause2.fromCode(null)).isNull();
        }

        @Test
        @DisplayName("빈 문자열 전달 → null 반환")
        void fromCode_blank_returnsNull() {
            assertThat(DescClause2.fromCode("")).isNull();
        }
    }
}
