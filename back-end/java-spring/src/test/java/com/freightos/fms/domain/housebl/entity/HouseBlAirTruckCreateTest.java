package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.VesselVoyage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HouseBlAir / HouseBlTruck 생성 비즈니스 규칙 단위 테스트")
class HouseBlAirTruckCreateTest {

    // ── HouseBlAir ────────────────────────────────────────────────────

    @Nested
    @DisplayName("HouseBlAir.create()")
    class HouseBlAirCreateTest {

        @Test
        @DisplayName("EXP Bound 생성 후 declaredValueCarriage 기본값 'N.V.D.'")
        void create_exp_declaredValueCarriageDefault() {
            HouseBlAir air = HouseBlAir.create(Bound.EXP);

            assertThat(air.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        }

        @Test
        @DisplayName("EXP Bound 생성 후 insurance 기본값 'NIL'")
        void create_exp_insuranceDefault() {
            HouseBlAir air = HouseBlAir.create(Bound.EXP);

            assertThat(air.getInsurance()).isEqualTo("NIL");
        }

        @Test
        @DisplayName("IMP Bound 생성 후 declaredValueCarriage 기본값 'N.V.D.'")
        void create_imp_declaredValueCarriageDefault() {
            HouseBlAir air = HouseBlAir.create(Bound.IMP);

            assertThat(air.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        }

        @Test
        @DisplayName("IMP Bound 생성 후 insurance 기본값 'NIL'")
        void create_imp_insuranceDefault() {
            HouseBlAir air = HouseBlAir.create(Bound.IMP);

            assertThat(air.getInsurance()).isEqualTo("NIL");
        }

        @Test
        @DisplayName("생성 직후 나머지 항공 필드는 null")
        void create_otherAirFieldsAreNull() {
            HouseBlAir air = HouseBlAir.create(Bound.EXP);

            assertThat(air.getAirlineCode()).isNull();
            assertThat(air.getChargeWeightKg()).isNull();
            assertThat(air.getDeclaredValueCustoms()).isNull();
        }
    }

    // ── HouseBlTruck ──────────────────────────────────────────────────

    @Nested
    @DisplayName("HouseBlTruck.create() — PRD §S-06: vesselName 'TRUCK' 고정")
    class HouseBlTruckCreateTest {

        @Test
        @DisplayName("create() 후 vesselVoyage.vesselName() == 'TRUCK'")
        void create_vesselNameIsTruck() {
            HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);

            assertThat(truck.getVesselVoyage()).isNotNull();
            assertThat(truck.getVesselVoyage().vesselName()).isEqualTo("TRUCK");
        }

        @Test
        @DisplayName("create() 후 getVesselName() == 'TRUCK'")
        void create_getVesselNameReturnsTruck() {
            HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);

            assertThat(truck.getVesselName()).isEqualTo("TRUCK");
        }

        @Test
        @DisplayName("create() 후 vesselVoyage.vesselCode() == null (코드 미설정)")
        void create_vesselCodeIsNull() {
            HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);

            assertThat(truck.getVesselVoyage().vesselCode()).isNull();
        }

        @Test
        @DisplayName("create() 후 voyageNo == null (항차 미설정)")
        void create_voyageNoIsNull() {
            HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);

            assertThat(truck.getVesselVoyage().voyageNo()).isNull();
        }
    }

    @Nested
    @DisplayName("HouseBlTruck.updateTruckFields() — vesselName 'TRUCK' 강제")
    class HouseBlTruckUpdateTest {

        @Test
        @DisplayName("vesselVoyage != null 전달해도 vesselName은 'TRUCK'으로 강제")
        void updateTruckFields_vesselNameForcedToTruck() {
            HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
            // 임의의 vesselName을 가진 VesselVoyage를 넘겨도 강제 재할당됨
            VesselVoyage userInput = VesselVoyage.of("CODE1", "EVER GIVEN", "V001");

            HouseBlTruck.TruckFields fields = new HouseBlTruck.TruckFields(
                    userInput,
                    null, null,
                    null, null,
                    null, null,
                    null, null,
                    null);

            truck.updateTruckFields(fields);

            assertThat(truck.getVesselVoyage().vesselName()).isEqualTo("TRUCK");
        }

        @Test
        @DisplayName("vesselVoyage != null 전달 시 voyageNo는 사용자 입력값 유지")
        void updateTruckFields_voyageNoPreservedFromUserInput() {
            HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
            VesselVoyage userInput = VesselVoyage.of(null, "ANY VESSEL", "V999");

            HouseBlTruck.TruckFields fields = new HouseBlTruck.TruckFields(
                    userInput,
                    null, null,
                    null, null,
                    null, null,
                    null, null,
                    null);

            truck.updateTruckFields(fields);

            // vesselName은 강제, voyageNo는 사용자 입력값 그대로
            assertThat(truck.getVesselVoyage().vesselName()).isEqualTo("TRUCK");
            assertThat(truck.getVesselVoyage().voyageNo()).isEqualTo("V999");
        }

        @Test
        @DisplayName("vesselVoyage == null 전달 시 voyageNo는 null, vesselName은 'TRUCK'")
        void updateTruckFields_vesselVoyageNull_voyageNoNull() {
            HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);

            HouseBlTruck.TruckFields fields = new HouseBlTruck.TruckFields(
                    null,
                    null, null,
                    null, null,
                    null, null,
                    null, null,
                    null);

            truck.updateTruckFields(fields);

            assertThat(truck.getVesselVoyage().vesselName()).isEqualTo("TRUCK");
            assertThat(truck.getVesselVoyage().voyageNo()).isNull();
        }
    }
}
