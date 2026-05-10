package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * MasterBlJpaEntity 단위 테스트.
 * Step 2.2: desc 필드·replaceDesc 메서드 제거 — syncDims 동작으로 교체.
 */
class MasterBlJpaEntityTest {

    // ── syncDims ─────────────────────────────────────────────────────

    @Test
    @DisplayName("syncDims: null 입력해도 예외 없이 처리된다")
    void syncDims_nullInput_noException() {
        MasterBlJpaEntity entity = new MasterBlJpaEntity();
        entity.setJobDiv(MasterBlJobDiv.SEA);
        entity.setBound(Bound.EXP);

        assertThatCode(() -> entity.syncDims(null)).doesNotThrowAnyException();
        assertThat(entity.getDims()).isEmpty();
    }

    @Test
    @DisplayName("syncDims: 빈 리스트 입력 시 dims가 비워진다")
    void syncDims_emptyList_clearsDims() {
        MasterBlJpaEntity entity = new MasterBlJpaEntity();
        MasterBlDimJpaEntity dim = new MasterBlDimJpaEntity();
        entity.syncDims(List.of(dim));

        entity.syncDims(List.of());

        assertThat(entity.getDims()).isEmpty();
    }

    @Test
    @DisplayName("syncDims: 기존 dims 교체 후 새 dims 리스트 반영")
    void syncDims_replaceExisting_newListReflected() {
        MasterBlJpaEntity entity = new MasterBlJpaEntity();
        MasterBlDimJpaEntity dim1 = new MasterBlDimJpaEntity();
        MasterBlDimJpaEntity dim2 = new MasterBlDimJpaEntity();
        entity.syncDims(List.of(dim1));

        entity.syncDims(List.of(dim1, dim2));

        assertThat(entity.getDims()).hasSize(2);
    }
}
