package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * MasterBlAirJpaEntity 단위 테스트.
 * Step 4.2: dims 소유가 MasterBlAirJpaEntity로 이전됨. syncDims 동작 검증.
 */
class MasterBlAirJpaEntityTest {

    // ── syncDims ─────────────────────────────────────────────────────

    @Test
    @DisplayName("syncDims: 빈 리스트 입력 시 dims가 비워지고 예외 없이 처리된다")
    void syncDims_emptyInput_noException() {
        MasterBlAirJpaEntity entity = new MasterBlAirJpaEntity();

        assertThatCode(() -> entity.mergeDims(List.of())).doesNotThrowAnyException();
        assertThat(entity.getDims()).isEmpty();
    }

    @Test
    @DisplayName("syncDims: 빈 리스트 입력 시 dims가 비워진다")
    void syncDims_emptyList_clearsDims() {
        MasterBlAirJpaEntity entity = new MasterBlAirJpaEntity();
        MasterBlDimJpaEntity dim = new MasterBlDimJpaEntity();
        entity.mergeDims(List.of(dim));

        entity.mergeDims(List.of());

        assertThat(entity.getDims()).isEmpty();
    }

    @Test
    @DisplayName("syncDims: 기존 dims 교체 후 새 dims 리스트 반영")
    void syncDims_replaceExisting_newListReflected() {
        MasterBlAirJpaEntity entity = new MasterBlAirJpaEntity();
        MasterBlDimJpaEntity dim1 = new MasterBlDimJpaEntity();
        MasterBlDimJpaEntity dim2 = new MasterBlDimJpaEntity();
        entity.mergeDims(List.of(dim1));

        entity.mergeDims(List.of(dim1, dim2));

        assertThat(entity.getDims()).hasSize(2);
    }
}
