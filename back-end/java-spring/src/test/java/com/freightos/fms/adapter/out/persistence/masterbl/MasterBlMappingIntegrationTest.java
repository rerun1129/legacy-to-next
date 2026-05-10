package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirChargeJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlScheduleLegJpaEntity;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.freightos.common.config.QueryDslConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Master B/L 단방향 @OneToMany 매핑의 CRUD·라운드트립·orphanRemoval·cascade 동작을 검증한다.
 * scheduleLegs는 Step 1.4에서 MasterBlJpaEntity → MasterBlAirJpaEntity 소유로 재배치됨.
 * @DataJpaTest 슬라이스 + H2 in-memory(application-test.yml).
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class MasterBlMappingIntegrationTest {

    @Autowired
    private EntityManager em;

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────────────

    private MasterBlJpaEntity newParent(MasterBlJobDiv jobDiv) {
        MasterBlJpaEntity p = new MasterBlJpaEntity();
        p.setJobDiv(jobDiv);
        p.setBound(Bound.EXP);
        return p;
    }

    private MasterBlAirJpaEntity newAirExt(MasterBlJpaEntity parent) {
        MasterBlAirJpaEntity a = new MasterBlAirJpaEntity();
        a.setMasterBl(parent);
        return a;
    }

    private MasterBlDimJpaEntity dim() {
        MasterBlDimJpaEntity d = new MasterBlDimJpaEntity();
        d.setQuantity(1);
        return d;
    }

    private MasterBlScheduleLegJpaEntity scheduleLeg(String toCode) {
        MasterBlScheduleLegJpaEntity leg = new MasterBlScheduleLegJpaEntity();
        leg.setToCode(toCode);
        leg.setOnBoardDt("20260101");
        leg.setArrivalDt("20260102");
        return leg;
    }

    private MasterBlAirChargeJpaEntity airCharge(String freightCode) {
        MasterBlAirChargeJpaEntity a = new MasterBlAirChargeJpaEntity();
        a.setFreightCode(freightCode);
        return a;
    }

    private MasterBlDescJpaEntity desc(MasterBlJpaEntity parent, String marks) {
        MasterBlDescJpaEntity d = new MasterBlDescJpaEntity();
        d.setMasterBl(parent);
        d.setMarks(marks);
        return d;
    }

    private long countScheduleLegs(Long masterBlAirId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM MasterBlScheduleLegJpaEntity c WHERE c.masterBlAirId = :pid", Long.class)
                .setParameter("pid", masterBlAirId)
                .getSingleResult();
    }

    private long countDims(Long masterBlId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM MasterBlDimJpaEntity c WHERE c.masterBlId = :pid", Long.class)
                .setParameter("pid", masterBlId)
                .getSingleResult();
    }

    private long countAirCharges(Long masterBlAirId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM MasterBlAirChargeJpaEntity c WHERE c.masterBlAirId = :pid", Long.class)
                .setParameter("pid", masterBlAirId)
                .getSingleResult();
    }

    // ── 테스트 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Sea 모드 save→load: dims 채움 라운드트립 (scheduleLegs/airCharges는 AIR 전용)")
    void seaMode_fullRoundTrip_dimsRestored() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.SEA);
        parent.syncDims(List.of(dim()));

        em.persist(parent);
        em.flush();
        em.clear();

        MasterBlJpaEntity loaded = em.find(MasterBlJpaEntity.class, parent.getMasterBlId());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getDims()).hasSize(1);
    }

    @Test
    @DisplayName("Air 모드 save→load: dims 채움 + airExt 통해 scheduleLegs/airCharges 라운드트립")
    void airMode_fullRoundTrip_allCollectionsRestored() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        parent.syncDims(List.of(dim()));
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        airExt.syncScheduleLegs(List.of(scheduleLeg("KRPUS")));
        airExt.syncAirCharges(List.of(airCharge("FUEL")));
        em.persist(airExt);
        em.flush();
        em.clear();

        MasterBlJpaEntity loaded = em.find(MasterBlJpaEntity.class, parent.getMasterBlId());
        MasterBlAirJpaEntity loadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parent.getMasterBlId())
                .getSingleResult();

        assertThat(loaded.getDims()).hasSize(1);
        assertThat(loadedAir.getScheduleLegs()).hasSize(1);
        assertThat(loadedAir.getAirCharges()).hasSize(1);
    }

    @Test
    @DisplayName("빈 컬렉션 저장/조회: NPE 없음, dims 빈 List 반환")
    void emptyCollections_noPeNpeAndEmptyListReturned() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.SEA);

        em.persist(parent);
        em.flush();
        em.clear();

        MasterBlJpaEntity loaded = em.find(MasterBlJpaEntity.class, parent.getMasterBlId());

        assertThat(loaded.getDims()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("자식 일부 필드 수정: dims[0].setQuantity → flush → 재조회 시 변경 반영")
    void partialChildUpdate_dimQuantity_persisted() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        MasterBlDimJpaEntity firstDim = dim();
        MasterBlDimJpaEntity secondDim = dim();
        parent.syncDims(List.of(firstDim, secondDim));

        em.persist(parent);
        em.flush();
        em.clear();

        MasterBlJpaEntity loaded = em.find(MasterBlJpaEntity.class, parent.getMasterBlId());
        Long firstDimId = loaded.getDims().get(0).getMasterBlDimId();
        loaded.getDims().get(0).setQuantity(99);

        em.flush();
        em.clear();

        MasterBlJpaEntity reloaded = em.find(MasterBlJpaEntity.class, parent.getMasterBlId());
        MasterBlDimJpaEntity updatedDim = reloaded.getDims().stream()
                .filter(d -> d.getMasterBlDimId().equals(firstDimId))
                .findFirst().orElseThrow();
        MasterBlDimJpaEntity otherDim = reloaded.getDims().stream()
                .filter(d -> !d.getMasterBlDimId().equals(firstDimId))
                .findFirst().orElseThrow();

        assertThat(updatedDim.getQuantity()).isEqualTo(99);
        assertThat(otherDim.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("syncDims: 기존 2건 → 신규 3건 교체. flush 후 자식 row count == 3, 기존 ID 모두 사라짐")
    void syncDims_replaceTwoWithThree_orphanRemovedAndNewRowsInserted() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        parent.syncDims(List.of(dim(), dim()));

        em.persist(parent);
        em.flush();
        em.clear();

        MasterBlJpaEntity loaded = em.find(MasterBlJpaEntity.class, parent.getMasterBlId());
        List<Long> oldIds = loaded.getDims().stream()
                .map(MasterBlDimJpaEntity::getMasterBlDimId)
                .toList();

        loaded.syncDims(List.of(dim(), dim(), dim()));
        em.flush();
        em.clear();

        MasterBlJpaEntity reloaded = em.find(MasterBlJpaEntity.class, parent.getMasterBlId());
        List<Long> newIds = reloaded.getDims().stream()
                .map(MasterBlDimJpaEntity::getMasterBlDimId)
                .toList();

        assertThat(reloaded.getDims()).hasSize(3);
        assertThat(newIds).doesNotContainAnyElementsOf(oldIds);
    }

    @Test
    @DisplayName("AIR ext syncScheduleLegs: 기존 1건 → 신규 2건 교체. orphanRemoval 동작 확인")
    void syncScheduleLegs_replaceOneWithTwo_orphanRemovedAndNewRowsInserted() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        airExt.syncScheduleLegs(List.of(scheduleLeg("USNYC")));
        em.persist(airExt);
        em.flush();
        em.clear();

        MasterBlAirJpaEntity loadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parent.getMasterBlId())
                .getSingleResult();
        List<Long> oldIds = loadedAir.getScheduleLegs().stream()
                .map(MasterBlScheduleLegJpaEntity::getMasterBlScheduleLegId)
                .toList();

        loadedAir.syncScheduleLegs(List.of(scheduleLeg("KRPUS"), scheduleLeg("JPOSA")));
        em.flush();
        em.clear();

        MasterBlAirJpaEntity reloadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parent.getMasterBlId())
                .getSingleResult();
        List<Long> newIds = reloadedAir.getScheduleLegs().stream()
                .map(MasterBlScheduleLegJpaEntity::getMasterBlScheduleLegId)
                .toList();

        assertThat(reloadedAir.getScheduleLegs()).hasSize(2);
        assertThat(newIds).doesNotContainAnyElementsOf(oldIds);
    }

    @Test
    @DisplayName("AIR ext syncAirCharges: 기존 2건 → 빈 리스트로 전체 삭제 (orphanRemoval)")
    void syncAirCharges_emptyList_allChildrenDeleted() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        airExt.syncAirCharges(List.of(airCharge("FUEL"), airCharge("AWC")));
        em.persist(airExt);
        em.flush();
        em.clear();

        MasterBlAirJpaEntity loadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parent.getMasterBlId())
                .getSingleResult();
        Long airExtId = loadedAir.getMasterBlAirId();

        loadedAir.syncAirCharges(List.of());
        em.flush();
        em.clear();

        assertThat(countAirCharges(airExtId)).isZero();
    }

    @Test
    @DisplayName("AIR ext delete → scheduleLegs/airCharges 자식 row count 모두 0 (cascade)")
    void airExtDelete_cascadeDeleteScheduleLegsAndAirCharges() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        parent.syncDims(List.of(dim()));
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        airExt.syncScheduleLegs(List.of(scheduleLeg("USNYC")));
        airExt.syncAirCharges(List.of(airCharge("FUEL")));
        em.persist(airExt);
        em.flush();
        em.clear();

        Long parentId = parent.getMasterBlId();
        MasterBlAirJpaEntity loadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parentId)
                .getSingleResult();
        Long airExtId = loadedAir.getMasterBlAirId();

        em.remove(loadedAir);
        em.flush();
        em.clear();

        assertThat(em.find(MasterBlAirJpaEntity.class, airExtId)).isNull();
        assertThat(countScheduleLegs(airExtId)).isZero();
        assertThat(countAirCharges(airExtId)).isZero();
    }

    @Test
    @DisplayName("부모 delete → dims 자식 row count 0 (cascade). airCharges는 airExt cascade로 정리")
    void parentDelete_cascadeDeleteDims() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        parent.syncDims(List.of(dim()));

        em.persist(parent);
        em.flush();
        em.clear();

        Long parentId = parent.getMasterBlId();
        MasterBlJpaEntity toDelete = em.find(MasterBlJpaEntity.class, parentId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        assertThat(em.find(MasterBlJpaEntity.class, parentId)).isNull();
        assertThat(countDims(parentId)).isZero();
    }

    @Test
    @DisplayName("replaceDesc: 기존 desc 교체 후 flush → 기존 descId로 조회 시 null (orphanRemoval)")
    void replaceDesc_orphanDescIsDeletedFromDb() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.SEA);
        em.persist(parent);
        em.flush();

        MasterBlDescJpaEntity oldDesc = desc(parent, "OLD MARKS");
        em.persist(oldDesc);
        parent.replaceDesc(oldDesc);
        em.flush();
        em.clear();

        MasterBlJpaEntity loaded = em.find(MasterBlJpaEntity.class, parent.getMasterBlId());
        Long oldDescId = loaded.getDesc().getMasterBlDescId();

        // 1단계: null로 교체 → Hibernate가 DELETE를 먼저 실행하도록 보장
        // (INSERT→DELETE 순 flush로 인한 unique constraint 위반 방지)
        loaded.replaceDesc(null);
        em.flush();

        // 2단계: 새 desc 설정 → cascade=ALL에 의해 INSERT
        MasterBlDescJpaEntity newDesc = desc(loaded, "NEW MARKS");
        loaded.replaceDesc(newDesc);
        em.flush();
        em.clear();

        assertThat(em.find(MasterBlDescJpaEntity.class, oldDescId)).isNull();
        MasterBlJpaEntity reloaded = em.find(MasterBlJpaEntity.class, parent.getMasterBlId());
        assertThat(reloaded.getDesc().getMarks()).isEqualTo("NEW MARKS");
    }

    @Test
    @DisplayName("desc round-trip: marks/description/descClause1/descClause2/remark 저장 후 재조회 시 모든 필드 유지")
    void desc_roundTrip_allFieldsPreserved() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        em.persist(parent);
        em.flush();

        MasterBlDescJpaEntity descEntity = desc(parent, "ROUND-TRIP MARKS");
        descEntity.setDescription("ROUND-TRIP DESCRIPTION");
        descEntity.setDescClause1(DescClause1.A);
        descEntity.setDescClause2(DescClause2.A);
        descEntity.setRemark("ROUND-TRIP REMARK");
        em.persist(descEntity);
        parent.replaceDesc(descEntity);
        em.flush();
        em.clear();

        MasterBlJpaEntity loaded = em.find(MasterBlJpaEntity.class, parent.getMasterBlId());
        MasterBlDescJpaEntity loadedDesc = loaded.getDesc();

        assertThat(loadedDesc.getMarks()).isEqualTo("ROUND-TRIP MARKS");
        assertThat(loadedDesc.getDescription()).isEqualTo("ROUND-TRIP DESCRIPTION");
        assertThat(loadedDesc.getDescClause1()).isEqualTo(DescClause1.A);
        assertThat(loadedDesc.getDescClause2()).isEqualTo(DescClause2.A);
        assertThat(loadedDesc.getRemark()).isEqualTo("ROUND-TRIP REMARK");
    }
}
