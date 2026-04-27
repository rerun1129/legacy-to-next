package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MasterBlMapperTest {

    private final MasterBlMapper mapper = new MasterBlMapper();

    @Test
    @DisplayName("항공 Master B/L Domain→JPA 변환: MasterBlAirJpaEntity 타입이며 기본값 필드가 복사된다")
    void toJpa_airDomain_producesAirJpaEntity() {
        MasterBlAir domain = MasterBlAir.create(Bound.EXP);

        MasterBlAirJpaEntity jpa = (MasterBlAirJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(MasterBlAirJpaEntity.class);
        assertThat(jpa.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(jpa.getInsurance()).isEqualTo("NIL");
    }

    @Test
    @DisplayName("해상 Master B/L Domain→JPA 변환: MasterBlSeaJpaEntity 타입이다")
    void toJpa_seaDomain_producesSeaJpaEntity() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);

        MasterBlSeaJpaEntity jpa = (MasterBlSeaJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(MasterBlSeaJpaEntity.class);
    }
}
