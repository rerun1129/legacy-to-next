package com.freightos.fms.adapter.out.persistence.housebl.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

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

    // ── syncScheduleLegs ──────────────────────────────────────────────

    @Test
    @DisplayName("syncScheduleLegs: 여러 leg를 sync하면 모두 포함되고 리스트 참조가 유지된다")
    void syncScheduleLegs_multipleLegs_allPresentAndListIdentityPreserved() {
        HouseBlJpaEntity entity = new HouseBlJpaEntity();
        List<HouseBlScheduleLegJpaEntity> originalRef = entity.getScheduleLegs();
        HouseBlScheduleLegJpaEntity leg1 = new HouseBlScheduleLegJpaEntity();
        HouseBlScheduleLegJpaEntity leg2 = new HouseBlScheduleLegJpaEntity();

        entity.syncScheduleLegs(List.of(leg1, leg2));

        assertThat(entity.getScheduleLegs()).isSameAs(originalRef);
        assertThat(entity.getScheduleLegs()).containsExactly(leg1, leg2);
    }

    // ── replaceDesc ─────────────────────────────────────────────────

    @Test
    @DisplayName("replaceDesc: 기존 desc가 있으면 기존 desc의 houseBl back-ref를 null로 만든다")
    void replaceDesc_existingDesc_nullsOldBackRef() {
        HouseBlJpaEntity entity = new HouseBlJpaEntity();
        HouseBlDescJpaEntity oldDesc = new HouseBlDescJpaEntity();
        oldDesc.setHouseBl(entity);
        entity.replaceDesc(oldDesc);

        HouseBlDescJpaEntity newDesc = new HouseBlDescJpaEntity();
        entity.replaceDesc(newDesc);

        assertThat(oldDesc.getHouseBl()).isNull();
        assertThat(entity.getDesc()).isEqualTo(newDesc);
    }

    @Test
    @DisplayName("replaceDesc: null 입력 시 기존 desc의 back-ref를 null로 만들고 desc도 null 처리")
    void replaceDesc_nullInput_clearsExistingDesc() {
        HouseBlJpaEntity entity = new HouseBlJpaEntity();
        HouseBlDescJpaEntity existingDesc = new HouseBlDescJpaEntity();
        existingDesc.setHouseBl(entity);
        entity.replaceDesc(existingDesc);

        entity.replaceDesc(null);

        assertThat(existingDesc.getHouseBl()).isNull();
        assertThat(entity.getDesc()).isNull();
    }

    @Test
    @DisplayName("replaceDesc: 기존 desc가 없을 때 null 입력해도 예외 없이 처리된다")
    void replaceDesc_noExistingDesc_nullInput_noException() {
        HouseBlJpaEntity entity = new HouseBlJpaEntity();

        assertThatCode(() -> entity.replaceDesc(null)).doesNotThrowAnyException();
        assertThat(entity.getDesc()).isNull();
    }
}
