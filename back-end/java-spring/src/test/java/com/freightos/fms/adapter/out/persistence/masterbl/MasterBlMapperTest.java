package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Master B/L 도메인 엔티티 팩토리 메서드 검증.
 * 프로젝트 구조상 별도 Mapper/JpaEntity 계층 없이 도메인 엔티티가
 * JPA 엔티티를 겸하므로, create() 팩토리의 타입 및 기본값을 직접 검증한다.
 */
class MasterBlMapperTest {

    @Test
    @DisplayName("항공 Master B/L 도메인 엔티티 생성 시 MasterBlAir 타입이다")
    void toJpa_airDomain_mapsToMasterBlAirJpaEntity() {
        MasterBlAir air = MasterBlAir.create(Bound.EXP);

        assertThat(air).isInstanceOf(MasterBlAir.class);
        assertThat(air.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(air.getInsurance()).isEqualTo("NIL");
    }

    @Test
    @DisplayName("해상 Master B/L 도메인 엔티티 생성 시 MasterBlSea 타입이다")
    void toJpa_seaDomain_mapsToMasterBlSeaJpaEntity() {
        MasterBlSea sea = MasterBlSea.create(Bound.EXP);

        assertThat(sea).isInstanceOf(MasterBlSea.class);
    }
}
