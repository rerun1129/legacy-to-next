package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirChargeJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlLicenseJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlScheduleLegJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckOrderJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.freightos.fms.application.config.QueryDslConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * House B/L 단방향 @OneToMany 매핑의 CRUD·라운드트립·orphanRemoval·cascade 동작을 검증한다.
 * @DataJpaTest 슬라이스 + H2 in-memory(application-test.yml).
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class HouseBlMappingIntegrationTest {

    @Autowired
    private EntityManager em;

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────────────

    private HouseBlJpaEntity newParent(JobDiv jobDiv) {
        HouseBlJpaEntity p = new HouseBlJpaEntity();
        p.setJobDiv(jobDiv);
        p.setBound(Bound.EXP);
        return p;
    }

    private HouseBlContainerJpaEntity container(String containerNo) {
        return HouseBlContainerJpaEntity.of(containerNo, ContainerType.T20GP, 20);
    }

    private HouseBlDimJpaEntity dim() {
        HouseBlDimJpaEntity d = new HouseBlDimJpaEntity();
        d.setQuantity(1);
        return d;
    }

    private HouseBlLicenseJpaEntity license(String licenseNo) {
        HouseBlLicenseJpaEntity l = new HouseBlLicenseJpaEntity();
        l.setLicenseNo(licenseNo);
        return l;
    }

    private HouseBlScheduleLegJpaEntity scheduleLeg(String toCode) {
        HouseBlScheduleLegJpaEntity leg = new HouseBlScheduleLegJpaEntity();
        leg.setToCode(toCode);
        leg.setOnBoardDt("20260101");
        leg.setArrivalDt("20260102");
        return leg;
    }

    private HouseBlTruckOrderJpaEntity truckOrder(String truckNo) {
        HouseBlTruckOrderJpaEntity t = new HouseBlTruckOrderJpaEntity();
        t.setTruckNo(truckNo);
        return t;
    }

    private HouseBlAirChargeJpaEntity airCharge(String freightCode) {
        HouseBlAirChargeJpaEntity a = new HouseBlAirChargeJpaEntity();
        a.setFreightCode(freightCode);
        return a;
    }

    private HouseBlDescJpaEntity desc(HouseBlJpaEntity parent, String marks) {
        HouseBlDescJpaEntity d = new HouseBlDescJpaEntity();
        d.setHouseBl(parent);
        d.setMarks(marks);
        return d;
    }

    private long countChildren(String table, Long parentId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM " + table + " c WHERE c.houseBlId = :pid", Long.class)
                .setParameter("pid", parentId)
                .getSingleResult();
    }

    // ── 테스트 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Sea 모드 save→load: containers 2건 + dims 1건 + licenses 1건 라운드트립")
    void seaMode_fullRoundTrip_allChildrenRestored() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        parent.syncContainers(List.of(container("CONT001"), container("CONT002")));
        parent.syncDims(List.of(dim()));
        parent.syncLicenses(List.of(license("LIC-001")));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getContainers()).hasSize(2);
        assertThat(loaded.getDims()).hasSize(1);
        assertThat(loaded.getLicenses()).hasSize(1);
        assertThat(loaded.getScheduleLegs()).isEmpty();
        assertThat(loaded.getTruckOrders()).isEmpty();
        assertThat(loaded.getAirCharges()).isEmpty();
    }

    @Test
    @DisplayName("Air 모드 save→load: dims/scheduleLegs/licenses/airCharges 채움, containers/truckOrders 빈 목록")
    void airMode_fullRoundTrip_correctCollections() {
        HouseBlJpaEntity parent = newParent(JobDiv.AIR);
        parent.syncDims(List.of(dim()));
        parent.syncScheduleLegs(List.of(scheduleLeg("USNYC")));
        parent.syncLicenses(List.of(license("LIC-AIR")));
        parent.syncAirCharges(List.of(airCharge("FUEL")));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());

        assertThat(loaded.getContainers()).isEmpty();
        assertThat(loaded.getTruckOrders()).isEmpty();
        assertThat(loaded.getDims()).hasSize(1);
        assertThat(loaded.getScheduleLegs()).hasSize(1);
        assertThat(loaded.getLicenses()).hasSize(1);
        assertThat(loaded.getAirCharges()).hasSize(1);
    }

    @Test
    @DisplayName("Truck 모드 save→load: truckOrders/dims만 채움")
    void truckMode_roundTrip_truckOrdersAndDimsOnly() {
        HouseBlJpaEntity parent = newParent(JobDiv.TRUCK);
        parent.syncTruckOrders(List.of(truckOrder("TRUCK-1")));
        parent.syncDims(List.of(dim()));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());

        assertThat(loaded.getTruckOrders()).hasSize(1);
        assertThat(loaded.getDims()).hasSize(1);
        assertThat(loaded.getContainers()).isEmpty();
        assertThat(loaded.getScheduleLegs()).isEmpty();
        assertThat(loaded.getLicenses()).isEmpty();
        assertThat(loaded.getAirCharges()).isEmpty();
    }

    @Test
    @DisplayName("NonBl 모드 save→load: dims만 채움")
    void nonBlMode_roundTrip_dimsOnly() {
        HouseBlJpaEntity parent = newParent(JobDiv.NON_BL);
        parent.syncDims(List.of(dim()));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());

        assertThat(loaded.getDims()).hasSize(1);
        assertThat(loaded.getContainers()).isEmpty();
        assertThat(loaded.getScheduleLegs()).isEmpty();
        assertThat(loaded.getLicenses()).isEmpty();
        assertThat(loaded.getTruckOrders()).isEmpty();
        assertThat(loaded.getAirCharges()).isEmpty();
    }

    @Test
    @DisplayName("빈 컬렉션 저장/조회: NPE 없음, 모든 컬렉션이 빈 List 반환")
    void emptyCollections_noPeNpeAndEmptyListReturned() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        // 모든 컬렉션이 기본 빈 ArrayList 상태

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());

        assertThat(loaded.getContainers()).isNotNull().isEmpty();
        assertThat(loaded.getDims()).isNotNull().isEmpty();
        assertThat(loaded.getScheduleLegs()).isNotNull().isEmpty();
        assertThat(loaded.getLicenses()).isNotNull().isEmpty();
        assertThat(loaded.getTruckOrders()).isNotNull().isEmpty();
        assertThat(loaded.getAirCharges()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("자식 일부 필드 수정: containers[0].setSealNo1 → flush → 재조회 시 변경 반영, 다른 자식 unchanged")
    void partialChildUpdate_sealNo1_persisted_otherChildUnchanged() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        parent.syncContainers(List.of(container("CONT-A"), container("CONT-B")));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        // containerNo로 순서 불확실할 수 있으므로 특정 컨테이너를 찾아 수정
        HouseBlContainerJpaEntity targetContainer = loaded.getContainers().stream()
                .filter(c -> "CONT-A".equals(c.getContainerNo()))
                .findFirst().orElseThrow();
        targetContainer.setSealNo1("SEAL-001");

        em.flush();
        em.clear();

        HouseBlJpaEntity reloaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        HouseBlContainerJpaEntity updatedA = reloaded.getContainers().stream()
                .filter(c -> "CONT-A".equals(c.getContainerNo()))
                .findFirst().orElseThrow();
        HouseBlContainerJpaEntity unchangedB = reloaded.getContainers().stream()
                .filter(c -> "CONT-B".equals(c.getContainerNo()))
                .findFirst().orElseThrow();

        assertThat(updatedA.getSealNo1()).isEqualTo("SEAL-001");
        assertThat(unchangedB.getSealNo1()).isNull();
    }

    @Test
    @DisplayName("syncContainers(newList): 기존 2건 → 신규 3건 교체. flush 후 자식 row count == 3, 기존 ID 모두 사라짐")
    void syncContainers_replaceTwoWithThree_orphanRemovedAndNewRowsInserted() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        parent.syncContainers(List.of(container("OLD-1"), container("OLD-2")));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        List<Long> oldIds = loaded.getContainers().stream()
                .map(HouseBlContainerJpaEntity::getHouseBlContainerId)
                .toList();

        loaded.syncContainers(List.of(
                container("NEW-1"), container("NEW-2"), container("NEW-3")));
        em.flush();
        em.clear();

        HouseBlJpaEntity reloaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        List<Long> newIds = reloaded.getContainers().stream()
                .map(HouseBlContainerJpaEntity::getHouseBlContainerId)
                .toList();

        assertThat(reloaded.getContainers()).hasSize(3);
        // 기존 ID가 하나도 남아있지 않아야 함 (orphanRemoval)
        assertThat(newIds).doesNotContainAnyElementsOf(oldIds);
        assertThat(reloaded.getContainers())
                .extracting(HouseBlContainerJpaEntity::getContainerNo)
                .containsExactlyInAnyOrder("NEW-1", "NEW-2", "NEW-3");
    }

    @Test
    @DisplayName("syncContainers(emptyList): 기존 모든 자식 DELETE — DB row count == 0")
    void syncContainers_emptyList_allChildrenDeleted() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        parent.syncContainers(List.of(container("CONT-X"), container("CONT-Y")));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        loaded.syncContainers(List.of());
        em.flush();
        em.clear();

        long rowCount = countChildren("HouseBlContainerJpaEntity", parent.getHouseBlId());
        assertThat(rowCount).isZero();
    }

    @Test
    @DisplayName("부모 delete → House/Container/Dim/License/ScheduleLeg/TruckOrder/AirCharge 자식 row count 모두 0 (cascade)")
    void parentDelete_cascadeDeleteAllChildren() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        parent.syncContainers(List.of(container("CONT-DEL")));
        parent.syncDims(List.of(dim()));
        parent.syncLicenses(List.of(license("LIC-DEL")));
        parent.syncScheduleLegs(List.of(scheduleLeg("KRPUS")));
        parent.syncTruckOrders(List.of(truckOrder("TRUCK-DEL")));
        parent.syncAirCharges(List.of(airCharge("FUEL-DEL")));

        em.persist(parent);
        em.flush();
        em.clear();

        Long parentId = parent.getHouseBlId();
        HouseBlJpaEntity toDelete = em.find(HouseBlJpaEntity.class, parentId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        assertThat(em.find(HouseBlJpaEntity.class, parentId)).isNull();
        assertThat(countChildren("HouseBlContainerJpaEntity", parentId)).isZero();
        assertThat(countChildren("HouseBlDimJpaEntity", parentId)).isZero();
        assertThat(countChildren("HouseBlLicenseJpaEntity", parentId)).isZero();
        assertThat(countChildren("HouseBlScheduleLegJpaEntity", parentId)).isZero();
        assertThat(countChildren("HouseBlTruckOrderJpaEntity", parentId)).isZero();
        assertThat(countChildren("HouseBlAirChargeJpaEntity", parentId)).isZero();
    }

    @Test
    @DisplayName("replaceDesc: 기존 desc 교체 후 flush → 기존 descId로 조회 시 null (orphanRemoval)")
    void replaceDesc_orphanDescIsDeletedFromDb() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        em.persist(parent);
        em.flush();

        HouseBlDescJpaEntity oldDesc = desc(parent, "OLD MARKS");
        em.persist(oldDesc);
        parent.replaceDesc(oldDesc);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        Long oldDescId = loaded.getDesc().getHouseBlDescId();

        HouseBlDescJpaEntity newDesc = desc(loaded, "NEW MARKS");
        em.persist(newDesc);
        loaded.replaceDesc(newDesc);
        em.flush();
        em.clear();

        assertThat(em.find(HouseBlDescJpaEntity.class, oldDescId)).isNull();
        HouseBlJpaEntity reloaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        assertThat(reloaded.getDesc().getMarks()).isEqualTo("NEW MARKS");
    }

    @Test
    @DisplayName("syncDims: 2건 저장 후 3건으로 교체 → flush → DB count==3, 기존 ID 없음")
    void syncDims_replace_orphanRemovedAndNewRowsInserted() {
        HouseBlJpaEntity parent = newParent(JobDiv.AIR);
        parent.syncDims(List.of(dim(), dim()));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        List<Long> oldIds = loaded.getDims().stream()
                .map(HouseBlDimJpaEntity::getHouseBlDimId)
                .toList();

        loaded.syncDims(List.of(dim(), dim(), dim()));
        em.flush();
        em.clear();

        long count = countChildren("HouseBlDimJpaEntity", parent.getHouseBlId());
        assertThat(count).isEqualTo(3);

        HouseBlJpaEntity reloaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        List<Long> newIds = reloaded.getDims().stream()
                .map(HouseBlDimJpaEntity::getHouseBlDimId)
                .toList();
        assertThat(newIds).doesNotContainAnyElementsOf(oldIds);
    }

    @Test
    @DisplayName("syncScheduleLegs: 1건 저장 후 empty 전달 → flush → DB count==0")
    void syncScheduleLegs_emptyList_allDeletedFromDb() {
        HouseBlJpaEntity parent = newParent(JobDiv.AIR);
        parent.syncScheduleLegs(List.of(scheduleLeg("USNYC")));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        loaded.syncScheduleLegs(List.of());
        em.flush();
        em.clear();

        long count = countChildren("HouseBlScheduleLegJpaEntity", parent.getHouseBlId());
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("syncLicenses: 2건 저장 후 다른 2건으로 교체 → DB count==2, 기존 ID 없음")
    void syncLicenses_replaceTwo_orphanRemovedAndNewInserted() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        parent.syncLicenses(List.of(license("LIC-OLD-1"), license("LIC-OLD-2")));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        List<Long> oldIds = loaded.getLicenses().stream()
                .map(HouseBlLicenseJpaEntity::getHouseBlLicenseId)
                .toList();

        loaded.syncLicenses(List.of(license("LIC-NEW-1"), license("LIC-NEW-2")));
        em.flush();
        em.clear();

        long count = countChildren("HouseBlLicenseJpaEntity", parent.getHouseBlId());
        assertThat(count).isEqualTo(2);

        HouseBlJpaEntity reloaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        List<Long> newIds = reloaded.getLicenses().stream()
                .map(HouseBlLicenseJpaEntity::getHouseBlLicenseId)
                .toList();
        assertThat(newIds).doesNotContainAnyElementsOf(oldIds);
    }

    @Test
    @DisplayName("syncTruckOrders: 1건 저장 후 empty 전달 → flush → DB count==0")
    void syncTruckOrders_emptyList_allDeletedFromDb() {
        HouseBlJpaEntity parent = newParent(JobDiv.TRUCK);
        parent.syncTruckOrders(List.of(truckOrder("TRUCK-01")));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        loaded.syncTruckOrders(List.of());
        em.flush();
        em.clear();

        long count = countChildren("HouseBlTruckOrderJpaEntity", parent.getHouseBlId());
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("syncAirCharges: 1건 저장 후 다른 1건으로 교체 → DB count==1, 기존 ID 없음")
    void syncAirCharges_replaceOne_orphanRemovedAndNewInserted() {
        HouseBlJpaEntity parent = newParent(JobDiv.AIR);
        parent.syncAirCharges(List.of(airCharge("FUEL")));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        List<Long> oldIds = loaded.getAirCharges().stream()
                .map(HouseBlAirChargeJpaEntity::getHouseBlAirChargeId)
                .toList();

        loaded.syncAirCharges(List.of(airCharge("AWC")));
        em.flush();
        em.clear();

        long count = countChildren("HouseBlAirChargeJpaEntity", parent.getHouseBlId());
        assertThat(count).isEqualTo(1);

        HouseBlJpaEntity reloaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        List<Long> newIds = reloaded.getAirCharges().stream()
                .map(HouseBlAirChargeJpaEntity::getHouseBlAirChargeId)
                .toList();
        assertThat(newIds).doesNotContainAnyElementsOf(oldIds);
    }
}
