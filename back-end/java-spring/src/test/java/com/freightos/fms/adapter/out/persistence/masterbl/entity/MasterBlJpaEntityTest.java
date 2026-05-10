package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MasterBlJpaEntity 단위 테스트.
 * Step 4.2: dims 소유가 MasterBlAirJpaEntity로 이전 — syncDims 검증은 MasterBlAirJpaEntityTest로 이동.
 */
class MasterBlJpaEntityTest {

    @Test
    @DisplayName("setJobDiv/setBound: 기본 필드 세터가 정상 동작한다")
    void setFields_basicSetterWorks() {
        MasterBlJpaEntity entity = new MasterBlJpaEntity();
        entity.setJobDiv(MasterBlJobDiv.AIR);
        entity.setBound(Bound.EXP);

        assertThat(entity.getJobDiv()).isEqualTo(MasterBlJobDiv.AIR);
        assertThat(entity.getBound()).isEqualTo(Bound.EXP);
    }
}
