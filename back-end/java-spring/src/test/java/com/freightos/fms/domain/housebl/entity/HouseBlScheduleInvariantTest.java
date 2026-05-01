package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.PortCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("HouseBl.updateSchedule invariant 단위 테스트")
class HouseBlScheduleInvariantTest {

    private HouseBlSea hbl;
    private PortCode pol;
    private PortCode pod;

    @BeforeEach
    void setUp() {
        hbl = HouseBlSea.create(Bound.EXP);
        pol = PortCode.of("KRPUS");
        pod = PortCode.of("USLAX");
    }

    @Test
    @DisplayName("etd < eta — 정상 케이스, 예외 없음")
    void updateSchedule_etdBeforeEta_noException() {
        BlDate etd = BlDate.of("20260101");
        BlDate eta = BlDate.of("20260201");

        assertThatNoException().isThrownBy(() -> hbl.updateSchedule(pol, pod, etd, eta));

        assertThat(hbl.getEtd()).isEqualTo(etd);
        assertThat(hbl.getEta()).isEqualTo(eta);
    }

    @Test
    @DisplayName("etd == eta — 경계값, 예외 없음")
    void updateSchedule_etdEqualsEta_noException() {
        BlDate etd = BlDate.of("20260115");
        BlDate eta = BlDate.of("20260115");

        assertThatNoException().isThrownBy(() -> hbl.updateSchedule(pol, pod, etd, eta));

        assertThat(hbl.getEtd()).isEqualTo(etd);
        assertThat(hbl.getEta()).isEqualTo(eta);
    }

    @Test
    @DisplayName("etd > eta — IllegalArgumentException, 메시지 검증")
    void updateSchedule_etdAfterEta_throwsIae() {
        BlDate etd = BlDate.of("20260201");
        BlDate eta = BlDate.of("20260101");

        assertThatThrownBy(() -> hbl.updateSchedule(pol, pod, etd, eta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("etd must be before or equal to eta");
    }

    @Test
    @DisplayName("etd == null — null 검증 스킵, 예외 없음")
    void updateSchedule_etdNull_skipValidation() {
        BlDate eta = BlDate.of("20260201");

        assertThatNoException().isThrownBy(() -> hbl.updateSchedule(pol, pod, null, eta));

        assertThat(hbl.getEtd()).isNull();
        assertThat(hbl.getEta()).isEqualTo(eta);
    }

    @Test
    @DisplayName("eta == null — null 검증 스킵, 예외 없음")
    void updateSchedule_etaNull_skipValidation() {
        BlDate etd = BlDate.of("20260101");

        assertThatNoException().isThrownBy(() -> hbl.updateSchedule(pol, pod, etd, null));

        assertThat(hbl.getEtd()).isEqualTo(etd);
        assertThat(hbl.getEta()).isNull();
    }

    @Test
    @DisplayName("etd == null, eta == null — 둘 다 null, 예외 없음")
    void updateSchedule_bothNull_noException() {
        assertThatNoException().isThrownBy(() -> hbl.updateSchedule(pol, pod, null, null));

        assertThat(hbl.getEtd()).isNull();
        assertThat(hbl.getEta()).isNull();
    }

    @Test
    @DisplayName("polCode, podCode 정상 할당 확인")
    void updateSchedule_portsAssigned() {
        hbl.updateSchedule(pol, pod, null, null);

        assertThat(hbl.getPolCode()).isEqualTo(pol);
        assertThat(hbl.getPodCode()).isEqualTo(pod);
    }
}
