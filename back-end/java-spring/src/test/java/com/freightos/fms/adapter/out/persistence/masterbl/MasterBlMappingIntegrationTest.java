package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirChargeJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlScheduleLegJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Master B/L 단방향 @OneToMany 매핑의 CRUD·라운드트립·orphanRemoval·cascade 동작을 검증한다.
 * scheduleLegs는 Step 1.4에서 MasterBlJpaEntity → MasterBlAirJpaEntity 소유로 재배치됨.
 * desc는 Step 2.2에서 master_bl_sea_desc / master_bl_air_desc 독립 테이블로 분리됨.
 * @DataJpaTest 슬라이스 + H2 in-memory(application-test.yml).
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class MasterBlMappingIntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private MasterBlSeaDescRepository masterBlSeaDescRepository;

    @Autowired
    private MasterBlAirDescRepository masterBlAirDescRepository;

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────────────

    private MasterBlJpaEntity newParent(MasterBlJobDiv jobDiv) {
        MasterBlJpaEntity p = new MasterBlJpaEntity();
        p.setJobDiv(jobDiv);
        p.setBound(Bound.EXP);
        return p;
    }

    private MasterBlSeaJpaEntity newSeaExt(MasterBlJpaEntity parent) {
        MasterBlSeaJpaEntity s = new MasterBlSeaJpaEntity();
        s.setMasterBl(parent);
        return s;
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

    private MasterBlSeaDescJpaEntity seaDesc(MasterBlSeaJpaEntity seaExt, String marks) {
        MasterBlSeaDescJpaEntity d = new MasterBlSeaDescJpaEntity();
        d.setSea(seaExt);
        d.setMarks(marks);
        return d;
    }

    private MasterBlAirDescJpaEntity airDesc(MasterBlAirJpaEntity airExt, String marks) {
        MasterBlAirDescJpaEntity d = new MasterBlAirDescJpaEntity();
        d.setAir(airExt);
        d.setMarks(marks);
        return d;
    }

    private long countScheduleLegs(Long masterBlAirId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM MasterBlScheduleLegJpaEntity c WHERE c.masterBlAirId = :pid", Long.class)
                .setParameter("pid", masterBlAirId)
                .getSingleResult();
    }

    private long countDims(Long masterBlAirId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM MasterBlDimJpaEntity c WHERE c.masterBlAirId = :pid", Long.class)
                .setParameter("pid", masterBlAirId)
                .getSingleResult();
    }

    private long countAirCharges(Long masterBlAirId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM MasterBlAirChargeJpaEntity c WHERE c.masterBlAirId = :pid", Long.class)
                .setParameter("pid", masterBlAirId)
                .getSingleResult();
    }

    private long countSeaDescs(Long masterBlSeaId) {
        return em.createQuery(
                        "SELECT COUNT(d) FROM MasterBlSeaDescJpaEntity d WHERE d.sea.masterBlSeaId = :sid", Long.class)
                .setParameter("sid", masterBlSeaId)
                .getSingleResult();
    }

    private long countAirDescs(Long masterBlAirId) {
        return em.createQuery(
                        "SELECT COUNT(d) FROM MasterBlAirDescJpaEntity d WHERE d.air.masterBlAirId = :aid", Long.class)
                .setParameter("aid", masterBlAirId)
                .getSingleResult();
    }

    // ── 테스트 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Sea 모드 save→load: SEA는 dims 미사용. parent 저장 후 조회 시 정상 로드 확인")
    void seaMode_fullRoundTrip_parentSavedAndLoaded() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.SEA);

        em.persist(parent);
        em.flush();
        em.clear();

        MasterBlJpaEntity loaded = em.find(MasterBlJpaEntity.class, parent.getMasterBlId());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getJobDiv()).isEqualTo(MasterBlJobDiv.SEA);
    }

    @Test
    @DisplayName("Air 모드 save→load: airExt 통해 dims/scheduleLegs/airCharges 라운드트립")
    void airMode_fullRoundTrip_allCollectionsRestored() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        airExt.syncDims(List.of(dim()));
        airExt.syncScheduleLegs(List.of(scheduleLeg("KRPUS")));
        airExt.syncAirCharges(List.of(airCharge("FUEL")));
        em.persist(airExt);
        em.flush();
        em.clear();

        MasterBlAirJpaEntity loadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parent.getMasterBlId())
                .getSingleResult();

        assertThat(loadedAir.getDims()).hasSize(1);
        assertThat(loadedAir.getScheduleLegs()).hasSize(1);
        assertThat(loadedAir.getAirCharges()).hasSize(1);
    }

    @Test
    @DisplayName("AIR airExt 빈 컬렉션 저장/조회: NPE 없음, dims 빈 List 반환")
    void emptyCollections_noPeNpeAndEmptyListReturned() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();
        em.clear();

        MasterBlAirJpaEntity loadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parent.getMasterBlId())
                .getSingleResult();

        assertThat(loadedAir.getDims()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("자식 일부 필드 수정: airExt.dims[0].setQuantity → flush → 재조회 시 변경 반영")
    void partialChildUpdate_dimQuantity_persisted() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        airExt.syncDims(List.of(dim(), dim()));
        em.persist(airExt);
        em.flush();
        em.clear();

        MasterBlAirJpaEntity loadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parent.getMasterBlId())
                .getSingleResult();
        Long firstDimId = loadedAir.getDims().get(0).getMasterBlDimId();
        loadedAir.getDims().get(0).setQuantity(99);

        em.flush();
        em.clear();

        MasterBlAirJpaEntity reloadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parent.getMasterBlId())
                .getSingleResult();
        MasterBlDimJpaEntity updatedDim = reloadedAir.getDims().stream()
                .filter(d -> d.getMasterBlDimId().equals(firstDimId))
                .findFirst().orElseThrow();
        MasterBlDimJpaEntity otherDim = reloadedAir.getDims().stream()
                .filter(d -> !d.getMasterBlDimId().equals(firstDimId))
                .findFirst().orElseThrow();

        assertThat(updatedDim.getQuantity()).isEqualTo(99);
        assertThat(otherDim.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("syncDims(airExt): 기존 2건 → 신규 3건 교체. flush 후 자식 row count == 3, 기존 ID 모두 사라짐")
    void syncDims_replaceTwoWithThree_orphanRemovedAndNewRowsInserted() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        airExt.syncDims(List.of(dim(), dim()));
        em.persist(airExt);
        em.flush();
        em.clear();

        MasterBlAirJpaEntity loadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parent.getMasterBlId())
                .getSingleResult();
        List<Long> oldIds = loadedAir.getDims().stream()
                .map(MasterBlDimJpaEntity::getMasterBlDimId)
                .toList();

        loadedAir.syncDims(List.of(dim(), dim(), dim()));
        em.flush();
        em.clear();

        MasterBlAirJpaEntity reloadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parent.getMasterBlId())
                .getSingleResult();
        List<Long> newIds = reloadedAir.getDims().stream()
                .map(MasterBlDimJpaEntity::getMasterBlDimId)
                .toList();

        assertThat(reloadedAir.getDims()).hasSize(3);
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
    @DisplayName("AIR ext delete → dims/scheduleLegs/airCharges 자식 row count 모두 0 (cascade)")
    void airExtDelete_cascadeDeleteScheduleLegsAndAirCharges() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        airExt.syncDims(List.of(dim()));
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
        assertThat(countDims(airExtId)).isZero();
        assertThat(countScheduleLegs(airExtId)).isZero();
        assertThat(countAirCharges(airExtId)).isZero();
    }

    @Test
    @DisplayName("AIR airExt delete → dims 자식 row count 0 (ON DELETE CASCADE). airExt 삭제 시 dims 정리")
    void airExtDelete_cascadeDeleteDims() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        airExt.syncDims(List.of(dim()));
        em.persist(airExt);
        em.flush();
        em.clear();

        MasterBlAirJpaEntity loadedAir = em.createQuery(
                        "SELECT a FROM MasterBlAirJpaEntity a WHERE a.masterBl.masterBlId = :id",
                        MasterBlAirJpaEntity.class)
                .setParameter("id", parent.getMasterBlId())
                .getSingleResult();
        Long airExtId = loadedAir.getMasterBlAirId();

        em.remove(loadedAir);
        em.flush();
        em.clear();

        assertThat(em.find(MasterBlAirJpaEntity.class, airExtId)).isNull();
        assertThat(countDims(airExtId)).isZero();
    }

    // ── SEA desc 저장·교체·cascade 테스트 ────────────────────────────────

    @Test
    @DisplayName("SEA desc round-trip: marks/description/descClause1/descClause2 저장 후 재조회 시 모든 필드 유지")
    void seaDesc_roundTrip_allFieldsPreserved() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.SEA);
        em.persist(parent);
        em.flush();

        MasterBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();

        MasterBlSeaDescJpaEntity descEntity = seaDesc(seaExt, "SEA ROUND-TRIP MARKS");
        descEntity.setDescription("SEA ROUND-TRIP DESCRIPTION");
        descEntity.setDescClause1(DescClause1.A);
        descEntity.setDescClause2(DescClause2.A);
        em.persist(descEntity);
        em.flush();
        em.clear();

        MasterBlSeaDescJpaEntity loadedDesc = em.createQuery(
                        "SELECT d FROM MasterBlSeaDescJpaEntity d WHERE d.sea.masterBlSeaId = :sid",
                        MasterBlSeaDescJpaEntity.class)
                .setParameter("sid", seaExt.getMasterBlSeaId())
                .getSingleResult();

        assertThat(loadedDesc.getMarks()).isEqualTo("SEA ROUND-TRIP MARKS");
        assertThat(loadedDesc.getDescription()).isEqualTo("SEA ROUND-TRIP DESCRIPTION");
        assertThat(loadedDesc.getDescClause1()).isEqualTo(DescClause1.A);
        assertThat(loadedDesc.getDescClause2()).isEqualTo(DescClause2.A);
    }

    @Test
    @DisplayName("SEA desc replace: 기존 desc 삭제 후 신규 insert — 기존 descId로 조회 시 null")
    void seaDesc_replace_oldRowDeletedNewRowInserted() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.SEA);
        em.persist(parent);
        em.flush();

        MasterBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();

        MasterBlSeaDescJpaEntity oldDesc = seaDesc(seaExt, "OLD SEA MARKS");
        em.persist(oldDesc);
        em.flush();
        Long oldDescId = oldDesc.getMasterBlSeaDescId();
        em.clear();

        // old desc 삭제 후 신규 insert
        MasterBlSeaDescJpaEntity toDelete = em.find(MasterBlSeaDescJpaEntity.class, oldDescId);
        em.remove(toDelete);
        em.flush();

        MasterBlSeaJpaEntity reloadedSea = em.find(MasterBlSeaJpaEntity.class, seaExt.getMasterBlSeaId());
        MasterBlSeaDescJpaEntity newDesc = seaDesc(reloadedSea, "NEW SEA MARKS");
        em.persist(newDesc);
        em.flush();
        em.clear();

        assertThat(em.find(MasterBlSeaDescJpaEntity.class, oldDescId)).isNull();
        MasterBlSeaDescJpaEntity loadedNew = em.createQuery(
                        "SELECT d FROM MasterBlSeaDescJpaEntity d WHERE d.sea.masterBlSeaId = :sid",
                        MasterBlSeaDescJpaEntity.class)
                .setParameter("sid", seaExt.getMasterBlSeaId())
                .getSingleResult();
        assertThat(loadedNew.getMarks()).isEqualTo("NEW SEA MARKS");
    }

    @Test
    @DisplayName("SEA ext delete — seaDesc를 명시 삭제 후 seaExt 삭제 (DDL_RULES §5 준수: DB CASCADE 금지)")
    void seaExtDelete_explicitDescDeleteThenExtDelete() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.SEA);
        em.persist(parent);
        em.flush();

        MasterBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();
        Long seaExtId = seaExt.getMasterBlSeaId();

        MasterBlSeaDescJpaEntity desc = seaDesc(seaExt, "EXPLICIT DELETE TEST");
        em.persist(desc);
        em.flush();
        em.clear();

        assertThat(countSeaDescs(seaExtId)).isEqualTo(1L);

        // 애플리케이션 레벨에서 명시 삭제: desc → ext 순서
        masterBlSeaDescRepository.deleteBySea_MasterBlSeaId(seaExtId);
        em.flush();
        MasterBlSeaJpaEntity loadedSea = em.find(MasterBlSeaJpaEntity.class, seaExtId);
        em.remove(loadedSea);
        em.flush();
        em.clear();

        assertThat(em.find(MasterBlSeaJpaEntity.class, seaExtId)).isNull();
        assertThat(countSeaDescs(seaExtId)).isZero();
    }

    // ── AIR desc 저장·교체·cascade 테스트 ────────────────────────────────

    @Test
    @DisplayName("AIR desc round-trip: marks/description/descClause1/descClause2 저장 후 재조회 시 모든 필드 유지")
    void airDesc_roundTrip_allFieldsPreserved() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();

        MasterBlAirDescJpaEntity descEntity = airDesc(airExt, "AIR ROUND-TRIP MARKS");
        descEntity.setDescription("AIR ROUND-TRIP DESCRIPTION");
        descEntity.setDescClause1(DescClause1.A);
        descEntity.setDescClause2(DescClause2.A);
        em.persist(descEntity);
        em.flush();
        em.clear();

        MasterBlAirDescJpaEntity loadedDesc = em.createQuery(
                        "SELECT d FROM MasterBlAirDescJpaEntity d WHERE d.air.masterBlAirId = :aid",
                        MasterBlAirDescJpaEntity.class)
                .setParameter("aid", airExt.getMasterBlAirId())
                .getSingleResult();

        assertThat(loadedDesc.getMarks()).isEqualTo("AIR ROUND-TRIP MARKS");
        assertThat(loadedDesc.getDescription()).isEqualTo("AIR ROUND-TRIP DESCRIPTION");
        assertThat(loadedDesc.getDescClause1()).isEqualTo(DescClause1.A);
        assertThat(loadedDesc.getDescClause2()).isEqualTo(DescClause2.A);
    }

    @Test
    @DisplayName("AIR ext delete — airDesc를 명시 삭제 후 airExt 삭제 (DDL_RULES §5 준수: DB CASCADE 금지)")
    void airExtDelete_explicitDescDeleteThenExtDelete() {
        MasterBlJpaEntity parent = newParent(MasterBlJobDiv.AIR);
        em.persist(parent);
        em.flush();

        MasterBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();
        Long airExtId = airExt.getMasterBlAirId();

        MasterBlAirDescJpaEntity desc = airDesc(airExt, "AIR EXPLICIT DELETE TEST");
        em.persist(desc);
        em.flush();
        em.clear();

        assertThat(countAirDescs(airExtId)).isEqualTo(1L);

        // 애플리케이션 레벨에서 명시 삭제: desc → ext 순서 (dims/scheduleLegs/airCharges는 ORM cascade로 정리)
        masterBlAirDescRepository.deleteByAir_MasterBlAirId(airExtId);
        em.flush();
        MasterBlAirJpaEntity loadedAir = em.find(MasterBlAirJpaEntity.class, airExtId);
        em.remove(loadedAir);
        em.flush();
        em.clear();

        assertThat(em.find(MasterBlAirJpaEntity.class, airExtId)).isNull();
        assertThat(countAirDescs(airExtId)).isZero();
    }
}
