package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlNonBl;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.fms.domain.housebl.enums.Bound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * House B/L 도메인 엔티티 팩토리 메서드 및 게터 검증.
 * 이 프로젝트에서는 도메인 엔티티가 JPA 엔티티를 직접 겸하므로
 * 별도 Mapper/JpaEntity 계층이 없다. create() 팩토리와 기본값 필드를 검증한다.
 *
 * 회귀 검출 포인트:
 *   - HouseBlSea.isTriangle() 이 NPE 없이 false 를 반환해야 한다.
 *   - HouseBlAir.create() 에서 declaredValueCarriage / insurance 기본값이 설정되어야 한다.
 *   - HouseBlTruck.create() 에서 vesselName 이 "TRUCK" 으로 고정되어야 한다.
 */
class HouseBlMapperTest {

    @Test
    @DisplayName("항공 도메인 엔티티 생성 시 기본값 필드가 올바르게 설정된다")
    void toJpa_airDomain_mapsAllAirFields() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);

        assertThat(air).isInstanceOf(HouseBlAir.class);
        assertThat(air.getAirlineCode()).isNull();
        assertThat(air.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(air.getInsurance()).isEqualTo("NIL");
    }

    @Test
    @DisplayName("해상 도메인 엔티티 생성 시 triangle/coLoad 기본값이 false 이다 (회귀 검출)")
    void toJpa_seaDomain_mapsSeaFields() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);

        assertThat(sea).isInstanceOf(HouseBlSea.class);
        // 핵심 회귀 검출 포인트: isTriangle() 이 NPE 없이 false 를 반환해야 한다
        assertThatCode(sea::isTriangle).doesNotThrowAnyException();
        assertThat(sea.isTriangle()).isFalse();
        assertThat(sea.isCoLoad()).isFalse();
    }

    @Test
    @DisplayName("트럭 도메인 엔티티 생성 시 vesselName 이 TRUCK 으로 고정된다")
    void toJpa_truckDomain_mapsVesselName() {
        HouseBlTruck truck = HouseBlTruck.create();

        assertThat(truck).isInstanceOf(HouseBlTruck.class);
        assertThat(truck.getVesselName()).isEqualTo("TRUCK");
    }

    @Test
    @DisplayName("Non-B/L 도메인 엔티티 생성 시 workDivision 이 지정값으로 설정된다")
    void toJpa_nonBlDomain_mapsWorkDivision() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA);

        assertThat(nonBl).isInstanceOf(HouseBlNonBl.class);
        assertThat(nonBl.getWorkDivision()).isEqualTo(HouseBlNonBl.WorkDivision.SEA);
    }
}
