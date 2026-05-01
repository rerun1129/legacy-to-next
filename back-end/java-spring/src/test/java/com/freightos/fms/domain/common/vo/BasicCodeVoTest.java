package com.freightos.fms.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("기본 코드형 VO 단위 테스트")
class BasicCodeVoTest {

    // ────────────────────────────────────────────────
    // BlNumber
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("BlNumber")
    class BlNumberTests {

        @Test
        @DisplayName("null 전달 시 NullPointerException")
        void constructor_null_throwsNpe() {
            assertThatThrownBy(() -> new BlNumber(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("BlNumber value must not be null");
        }

        @Test
        @DisplayName("blank 전달 시 IllegalArgumentException")
        void constructor_blank_throwsIae() {
            assertThatThrownBy(() -> new BlNumber(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("BlNumber value must not be blank");
        }

        @Test
        @DisplayName("정상 값으로 생성 후 value() 반환 확인")
        void constructor_valid_returnsValue() {
            BlNumber blNumber = new BlNumber("KOSEL123456");
            assertThat(blNumber.value()).isEqualTo("KOSEL123456");
        }
    }

    // ────────────────────────────────────────────────
    // MblNo
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("MblNo")
    class MblNoTests {

        @Test
        @DisplayName("null 전달 시 NullPointerException")
        void constructor_null_throwsNpe() {
            assertThatThrownBy(() -> new MblNo(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("MblNo value must not be null");
        }

        @Test
        @DisplayName("blank 전달 시 IllegalArgumentException")
        void constructor_blank_throwsIae() {
            assertThatThrownBy(() -> new MblNo(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("MblNo value must not be blank");
        }

        @Test
        @DisplayName("정상 값으로 생성 후 value() 반환 확인")
        void constructor_valid_returnsValue() {
            MblNo mblNo = new MblNo("MBLNO789");
            assertThat(mblNo.value()).isEqualTo("MBLNO789");
        }
    }

    // ────────────────────────────────────────────────
    // LinerCode
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("LinerCode")
    class LinerCodeTests {

        @Test
        @DisplayName("null 전달 시 NullPointerException")
        void constructor_null_throwsNpe() {
            assertThatThrownBy(() -> new LinerCode(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("LinerCode value must not be null");
        }

        @Test
        @DisplayName("blank 전달 시 IllegalArgumentException")
        void constructor_blank_throwsIae() {
            assertThatThrownBy(() -> new LinerCode(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("LinerCode value must not be blank");
        }

        @Test
        @DisplayName("정상 값으로 생성 후 value() 반환 확인")
        void constructor_valid_returnsValue() {
            LinerCode linerCode = new LinerCode("COSCO");
            assertThat(linerCode.value()).isEqualTo("COSCO");
        }
    }

    // ────────────────────────────────────────────────
    // EmployeeCode
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("EmployeeCode")
    class EmployeeCodeTests {

        @Test
        @DisplayName("null 전달 시 NullPointerException")
        void constructor_null_throwsNpe() {
            assertThatThrownBy(() -> new EmployeeCode(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("EmployeeCode value must not be null");
        }

        @Test
        @DisplayName("blank 전달 시 IllegalArgumentException")
        void constructor_blank_throwsIae() {
            assertThatThrownBy(() -> new EmployeeCode(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EmployeeCode value must not be blank");
        }

        @Test
        @DisplayName("정상 값으로 생성 후 value() 반환 확인")
        void constructor_valid_returnsValue() {
            EmployeeCode employeeCode = new EmployeeCode("EMP001");
            assertThat(employeeCode.value()).isEqualTo("EMP001");
        }
    }

    // ────────────────────────────────────────────────
    // TeamCode
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("TeamCode")
    class TeamCodeTests {

        @Test
        @DisplayName("null 전달 시 NullPointerException")
        void constructor_null_throwsNpe() {
            assertThatThrownBy(() -> new TeamCode(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("TeamCode value must not be null");
        }

        @Test
        @DisplayName("blank 전달 시 IllegalArgumentException")
        void constructor_blank_throwsIae() {
            assertThatThrownBy(() -> new TeamCode(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("TeamCode value must not be blank");
        }

        @Test
        @DisplayName("정상 값으로 생성 후 value() 반환 확인")
        void constructor_valid_returnsValue() {
            TeamCode teamCode = new TeamCode("TEAM_A");
            assertThat(teamCode.value()).isEqualTo("TEAM_A");
        }
    }

    // ────────────────────────────────────────────────
    // SealNumber
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("SealNumber")
    class SealNumberTests {

        @Test
        @DisplayName("null 전달 시 NullPointerException")
        void constructor_null_throwsNpe() {
            assertThatThrownBy(() -> new SealNumber(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("SealNumber value must not be null");
        }

        @Test
        @DisplayName("blank 전달 시 IllegalArgumentException")
        void constructor_blank_throwsIae() {
            assertThatThrownBy(() -> new SealNumber(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SealNumber value must not be blank");
        }

        @Test
        @DisplayName("정상 값으로 생성 후 value() 반환 확인")
        void constructor_valid_returnsValue() {
            SealNumber sealNumber = new SealNumber("SEAL9999");
            assertThat(sealNumber.value()).isEqualTo("SEAL9999");
        }
    }

    // ────────────────────────────────────────────────
    // AirlineCode
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("AirlineCode")
    class AirlineCodeTests {

        @Test
        @DisplayName("null 전달 시 NullPointerException")
        void constructor_null_throwsNpe() {
            assertThatThrownBy(() -> new AirlineCode(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("AirlineCode value must not be null");
        }

        @Test
        @DisplayName("blank 전달 시 IllegalArgumentException")
        void constructor_blank_throwsIae() {
            assertThatThrownBy(() -> new AirlineCode(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("AirlineCode value must not be blank");
        }

        @Test
        @DisplayName("정상 값으로 생성 후 value() 반환 확인")
        void constructor_valid_returnsValue() {
            AirlineCode airlineCode = new AirlineCode("KE");
            assertThat(airlineCode.value()).isEqualTo("KE");
        }
    }

    // ────────────────────────────────────────────────
    // PortCode
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("PortCode")
    class PortCodeTests {

        @Test
        @DisplayName("null 전달 시 IllegalArgumentException (PortCode는 null 체크에 IAE 사용)")
        void constructor_null_throwsIae() {
            assertThatThrownBy(() -> new PortCode(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PortCode value must not be blank");
        }

        @Test
        @DisplayName("blank 전달 시 IllegalArgumentException")
        void constructor_blank_throwsIae() {
            assertThatThrownBy(() -> new PortCode(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PortCode value must not be blank");
        }

        @Test
        @DisplayName("정상 값으로 생성 후 value() 반환 확인")
        void constructor_valid_returnsValue() {
            PortCode portCode = new PortCode("KRPUS");
            assertThat(portCode.value()).isEqualTo("KRPUS");
        }
    }

    // ────────────────────────────────────────────────
    // AirportCode
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("AirportCode")
    class AirportCodeTests {

        @Test
        @DisplayName("null 전달 시 IllegalArgumentException (AirportCode는 null 체크에 IAE 사용)")
        void constructor_null_throwsIae() {
            assertThatThrownBy(() -> new AirportCode(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("AirportCode value must not be blank");
        }

        @Test
        @DisplayName("blank 전달 시 IllegalArgumentException")
        void constructor_blank_throwsIae() {
            assertThatThrownBy(() -> new AirportCode(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("AirportCode value must not be blank");
        }

        @Test
        @DisplayName("정상 값으로 생성 후 value() 반환 확인")
        void constructor_valid_returnsValue() {
            AirportCode airportCode = new AirportCode("ICN");
            assertThat(airportCode.value()).isEqualTo("ICN");
        }
    }

    // ────────────────────────────────────────────────
    // VesselVoyage (vesselName 검증 중심)
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("VesselVoyage")
    class VesselVoyageTests {

        @Test
        @DisplayName("vesselName null 전달 시 NullPointerException")
        void constructor_vesselNameNull_throwsNpe() {
            assertThatThrownBy(() -> new VesselVoyage("CODE1", null, "001N"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("VesselVoyage.vesselName must not be null");
        }

        @Test
        @DisplayName("vesselName blank 전달 시 IllegalArgumentException")
        void constructor_vesselNameBlank_throwsIae() {
            assertThatThrownBy(() -> new VesselVoyage("CODE1", " ", "001N"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("VesselVoyage.vesselName must not be blank");
        }

        @Test
        @DisplayName("정상 값으로 생성 후 필드 검증")
        void constructor_valid_returnsFields() {
            VesselVoyage vesselVoyage = new VesselVoyage("HYMX", "HYUNDAI MARS", "001N");
            assertThat(vesselVoyage.vesselCode()).isEqualTo("HYMX");
            assertThat(vesselVoyage.vesselName()).isEqualTo("HYUNDAI MARS");
            assertThat(vesselVoyage.voyageNo()).isEqualTo("001N");
        }

        @Test
        @DisplayName("vesselCode와 voyageNo는 nullable — null 허용")
        void constructor_nullableFields_allowNull() {
            VesselVoyage vesselVoyage = new VesselVoyage(null, "SOME VESSEL", null);
            assertThat(vesselVoyage.vesselCode()).isNull();
            assertThat(vesselVoyage.voyageNo()).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // CustomerCode
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("CustomerCode")
    class CustomerCodeTests {

        @Test
        @DisplayName("value null 전달 시 NullPointerException")
        void constructor_valueNull_throwsNpe() {
            assertThatThrownBy(() -> new CustomerCode(null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("CustomerCode value must not be null");
        }

        @Test
        @DisplayName("value blank 전달 시 IllegalArgumentException")
        void constructor_valueBlank_throwsIae() {
            assertThatThrownBy(() -> new CustomerCode(" ", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CustomerCode value must not be blank");
        }

        @Test
        @DisplayName("address는 nullable — null address 정상 생성")
        void constructor_nullAddress_allowed() {
            CustomerCode customerCode = new CustomerCode("CUST001", null);
            assertThat(customerCode.value()).isEqualTo("CUST001");
            assertThat(customerCode.address()).isNull();
        }

        @Test
        @DisplayName("정상 값으로 생성 후 value()와 address() 반환 확인")
        void constructor_valid_returnsFields() {
            CustomerCode customerCode = new CustomerCode("CUST001", "123 Main St");
            assertThat(customerCode.value()).isEqualTo("CUST001");
            assertThat(customerCode.address()).isEqualTo("123 Main St");
        }
    }
}
