package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HouseBlJpaEntityTest {

    // ── HouseBlSeaJpaEntity.syncContainers ───────────────────────────

    @Test
    @DisplayName("HouseBlSeaJpaEntity.syncContainers: 기존 리스트 참조를 유지하면서 내용을 교체한다 (orphanRemoval 지원)")
    void seaSyncContainers_preservesListIdentity() {
        HouseBlSeaJpaEntity entity = new HouseBlSeaJpaEntity();
        List<HouseBlSeaContainerJpaEntity> originalRef = entity.getContainers();
        HouseBlSeaContainerJpaEntity c = new HouseBlSeaContainerJpaEntity();

        entity.syncContainers(List.of(c));

        assertThat(entity.getContainers()).isSameAs(originalRef);
        assertThat(entity.getContainers()).containsExactly(c);
    }

    @Test
    @DisplayName("HouseBlSeaJpaEntity.syncContainers: 빈 리스트로 sync하면 기존 컨테이너가 모두 제거된다")
    void seaSyncContainers_emptyInput_clearsExistingItems() {
        HouseBlSeaJpaEntity entity = new HouseBlSeaJpaEntity();
        HouseBlSeaContainerJpaEntity c = new HouseBlSeaContainerJpaEntity();
        entity.syncContainers(List.of(c));

        entity.syncContainers(List.of());

        assertThat(entity.getContainers()).isEmpty();
    }

    // ── HouseBlNonBlJpaEntity.mergeContainers ────────────────────────

    @Test
    @DisplayName("HouseBlNonBlJpaEntity.mergeContainers: id 없는 incoming은 신규 추가된다")
    void nonBlMergeContainers_newIncoming_isAdded() {
        HouseBlNonBlJpaEntity entity = new HouseBlNonBlJpaEntity();
        HouseBlNonBlContainerJpaEntity newContainer = new HouseBlNonBlContainerJpaEntity();

        entity.mergeContainers(List.of(newContainer));

        assertThat(entity.getContainers()).containsExactly(newContainer);
    }

    @Test
    @DisplayName("HouseBlNonBlJpaEntity.mergeContainers: 빈 리스트로 merge하면 기존 컨테이너가 모두 제거된다")
    void nonBlMergeContainers_emptyInput_clearsExistingItems() {
        HouseBlNonBlJpaEntity entity = new HouseBlNonBlJpaEntity();
        HouseBlNonBlContainerJpaEntity c = new HouseBlNonBlContainerJpaEntity();
        entity.mergeContainers(List.of(c));

        entity.mergeContainers(List.of());

        assertThat(entity.getContainers()).isEmpty();
    }

    // ── HouseBlJpaEntity.syncDims ─────────────────────────────────────

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
