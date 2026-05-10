package com.freightos.fms.adapter.out.persistence.housebl.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HouseBlJpaEntityTest {

    // ── syncContainers ────────────────────────────────────────────────

    @Test
    @DisplayName("syncContainers: 기존 리스트 참조를 유지하면서 내용을 교체한다 (orphanRemoval 지원)")
    void syncContainers_preservesListIdentity() {
        HouseBlJpaEntity entity = new HouseBlJpaEntity();
        List<HouseBlContainerJpaEntity> originalRef = entity.getContainers();
        HouseBlContainerJpaEntity c = HouseBlContainerJpaEntity.of("CONT001", null, 20);

        entity.syncContainers(List.of(c));

        assertThat(entity.getContainers()).isSameAs(originalRef);
        assertThat(entity.getContainers()).containsExactly(c);
    }

    @Test
    @DisplayName("syncContainers: 빈 리스트로 sync하면 기존 컨테이너가 모두 제거된다")
    void syncContainers_emptyInput_clearsExistingItems() {
        HouseBlJpaEntity entity = new HouseBlJpaEntity();
        entity.syncContainers(List.of(HouseBlContainerJpaEntity.of("CONT001", null, 20)));

        entity.syncContainers(List.of());

        assertThat(entity.getContainers()).isEmpty();
    }

    // ── syncDims ──────────────────────────────────────────────────────

    @Test
    @DisplayName("syncDims: 기존 리스트 참조를 유지하면서 내용을 교체한다")
    void syncDims_preservesListIdentity() {
        HouseBlJpaEntity entity = new HouseBlJpaEntity();
        List<HouseBlDimJpaEntity> originalRef = entity.getDims();
        HouseBlDimJpaEntity dim = new HouseBlDimJpaEntity();

        entity.syncDims(List.of(dim));

        assertThat(entity.getDims()).isSameAs(originalRef);
        assertThat(entity.getDims()).containsExactly(dim);
    }

    @Test
    @DisplayName("syncDims: 빈 리스트로 sync하면 기존 dim들이 모두 제거된다")
    void syncDims_emptyInput_clearsExistingCollection() {
        HouseBlJpaEntity entity = new HouseBlJpaEntity();
        HouseBlDimJpaEntity dim = new HouseBlDimJpaEntity();
        entity.syncDims(List.of(dim));

        entity.syncDims(List.of());

        assertThat(entity.getDims()).isEmpty();
    }

}

