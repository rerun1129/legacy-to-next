package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlNonBl;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HouseBlMapperTest {

    private final HouseBlMapper mapper = new HouseBlMapper();

    @Test
    @DisplayName("항공 Domain→JPA 변환: HouseBlAirJpaEntity 타입이며 기본값 필드가 복사된다")
    void toJpa_airDomain_producesAirJpaEntity() {
        HouseBlAir domain = HouseBlAir.create(Bound.EXP);

        HouseBlAirJpaEntity jpa = (HouseBlAirJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(HouseBlAirJpaEntity.class);
        assertThat(jpa.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(jpa.getInsurance()).isEqualTo("NIL");
    }

    @Test
    @DisplayName("해상 Domain→JPA 변환: isTriangle/isCoLoad 게터가 정상 동작한다 (회귀 검출)")
    void toJpa_seaDomain_triangleFlagsAreMapped() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);

        HouseBlSeaJpaEntity jpa = (HouseBlSeaJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(HouseBlSeaJpaEntity.class);
        // domain.isTriangle() 호출이 컴파일·런타임 모두 성공해야 함 — isIsTriangle() 오탈자 회귀 방지
        assertThat(jpa.isTriangle()).isFalse();
        assertThat(jpa.isCoLoad()).isFalse();
    }

    @Test
    @DisplayName("트럭 Domain→JPA 변환: vesselName 이 TRUCK 으로 고정된다")
    void toJpa_truckDomain_vesselNameIsTruck() {
        HouseBlTruck domain = HouseBlTruck.create();

        HouseBlTruckJpaEntity jpa = (HouseBlTruckJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(HouseBlTruckJpaEntity.class);
        assertThat(jpa.getVesselName()).isEqualTo("TRUCK");
    }

    @Test
    @DisplayName("Non-B/L Domain→JPA 변환: workDivision 이 매핑된다")
    void toJpa_nonBlDomain_workDivisionIsMapped() {
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA);

        HouseBlNonBlJpaEntity jpa = (HouseBlNonBlJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(HouseBlNonBlJpaEntity.class);
        assertThat(jpa.getWorkDivision()).isEqualTo(HouseBlNonBl.WorkDivision.SEA);
    }
}
