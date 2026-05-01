package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.PortCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("MasterBl.updateSchedule invariant + MasterBlAir 기본값 단위 테스트")
class MasterBlScheduleInvariantTest {

    @Nested
    @DisplayName("MasterBl.updateSchedule invariant")
    class UpdateScheduleTest {

        private MasterBlSea mbl;
        private PortCode pol;
        private PortCode pod;

        @BeforeEach
        void setUp() {
            mbl = MasterBlSea.create(Bound.EXP);
            pol = PortCode.of("KRPUS");
            pod = PortCode.of("USLAX");
        }

        @Test
        @DisplayName("etd < eta — 정상 케이스, 예외 없음")
        void updateSchedule_etdBeforeEta_noException() {
            BlDate etd = BlDate.of("20260101");
            BlDate eta = BlDate.of("20260201");

            assertThatNoException().isThrownBy(() -> mbl.updateSchedule(pol, pod, etd, eta));

            assertThat(mbl.getEtd()).isEqualTo(etd);
            assertThat(mbl.getEta()).isEqualTo(eta);
        }

        @Test
        @DisplayName("etd == eta — 경계값, 예외 없음")
        void updateSchedule_etdEqualsEta_noException() {
            BlDate etd = BlDate.of("20260115");
            BlDate eta = BlDate.of("20260115");

            assertThatNoException().isThrownBy(() -> mbl.updateSchedule(pol, pod, etd, eta));

            assertThat(mbl.getEtd()).isEqualTo(etd);
            assertThat(mbl.getEta()).isEqualTo(eta);
        }

        @Test
        @DisplayName("etd > eta — IllegalArgumentException, 메시지 검증")
        void updateSchedule_etdAfterEta_throwsIae() {
            BlDate etd = BlDate.of("20260201");
            BlDate eta = BlDate.of("20260101");

            assertThatThrownBy(() -> mbl.updateSchedule(pol, pod, etd, eta))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("etd must be before or equal to eta");
        }

        @Test
        @DisplayName("etd == null — null 검증 스킵, 예외 없음")
        void updateSchedule_etdNull_skipValidation() {
            BlDate eta = BlDate.of("20260201");

            assertThatNoException().isThrownBy(() -> mbl.updateSchedule(pol, pod, null, eta));

            assertThat(mbl.getEtd()).isNull();
            assertThat(mbl.getEta()).isEqualTo(eta);
        }

        @Test
        @DisplayName("eta == null — null 검증 스킵, 예외 없음")
        void updateSchedule_etaNull_skipValidation() {
            BlDate etd = BlDate.of("20260101");

            assertThatNoException().isThrownBy(() -> mbl.updateSchedule(pol, pod, etd, null));

            assertThat(mbl.getEtd()).isEqualTo(etd);
            assertThat(mbl.getEta()).isNull();
        }

        @Test
        @DisplayName("etd == null, eta == null — 둘 다 null, 예외 없음")
        void updateSchedule_bothNull_noException() {
            assertThatNoException().isThrownBy(() -> mbl.updateSchedule(pol, pod, null, null));

            assertThat(mbl.getEtd()).isNull();
            assertThat(mbl.getEta()).isNull();
        }

        @Test
        @DisplayName("polCode, podCode 정상 할당 확인")
        void updateSchedule_portsAssigned() {
            mbl.updateSchedule(pol, pod, null, null);

            assertThat(mbl.getPolCode()).isEqualTo(pol);
            assertThat(mbl.getPodCode()).isEqualTo(pod);
        }
    }

    // ── MasterBlAir 기본값 ────────────────────────────────────────────

    @Nested
    @DisplayName("MasterBlAir.create() 기본값 검증")
    class MasterBlAirCreateTest {

        @Test
        @DisplayName("EXP Bound 생성 후 declaredValueCarriage 기본값 'N.V.D.'")
        void create_exp_declaredValueCarriageDefault() {
            MasterBlAir air = MasterBlAir.create(Bound.EXP);

            assertThat(air.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        }

        @Test
        @DisplayName("EXP Bound 생성 후 insurance 기본값 'NIL'")
        void create_exp_insuranceDefault() {
            MasterBlAir air = MasterBlAir.create(Bound.EXP);

            assertThat(air.getInsurance()).isEqualTo("NIL");
        }

        @Test
        @DisplayName("IMP Bound 생성 후 declaredValueCarriage 기본값 'N.V.D.'")
        void create_imp_declaredValueCarriageDefault() {
            MasterBlAir air = MasterBlAir.create(Bound.IMP);

            assertThat(air.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        }

        @Test
        @DisplayName("IMP Bound 생성 후 insurance 기본값 'NIL'")
        void create_imp_insuranceDefault() {
            MasterBlAir air = MasterBlAir.create(Bound.IMP);

            assertThat(air.getInsurance()).isEqualTo("NIL");
        }

        @Test
        @DisplayName("생성 직후 나머지 항공 필드는 null")
        void create_otherAirFieldsAreNull() {
            MasterBlAir air = MasterBlAir.create(Bound.EXP);

            assertThat(air.getAirlineCode()).isNull();
            assertThat(air.getChargeWeightKg()).isNull();
            assertThat(air.getDeclaredValueCustoms()).isNull();
            assertThat(air.getSecurityStatus()).isNull();
            assertThat(air.getFlightType()).isNull();
        }
    }
}
