package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirChargeJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlScheduleLegJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckOrderJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.HouseBlNonBlRepository;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import jakarta.persistence.EntityManager;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.freightos.common.config.QueryDslConfig;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * House B/L 단방향 @OneToMany 매핑의 CRUD·라운드트립·orphanRemoval·cascade 동작을 검증한다.
 * @DataJpaTest 슬라이스 + H2 in-memory(application-test.yml).
 *
 * SEA 컨테이너 → HouseBlSeaJpaEntity 소유 (house_bl_sea_container).
 * NON_BL 컨테이너 → HouseBlNonBlJpaEntity 소유 (house_bl_nonbl_container).
 *
 * StatementInspector 기반 SQL 캡처 회귀 테스트가 포함된다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, HouseBlMappingIntegrationTest.InspectorConfig.class})
@Transactional
class HouseBlMappingIntegrationTest {

    /**
     * 실행된 SQL을 캡처하는 StatementInspector 구현체.
     * HibernatePropertiesCustomizer로 Hibernate에 등록한다.
     */
    static class SqlCapturingInspector implements StatementInspector {
        private final List<String> capturedSql = new ArrayList<>();

        @Override
        public String inspect(String sql) {
            capturedSql.add(sql);
            return sql;
        }

        List<String> getCapturedSql() { return capturedSql; }
        void reset() { capturedSql.clear(); }

        long countContaining(String keyword) {
            String lower = keyword.toLowerCase();
            return capturedSql.stream().filter(s -> s.toLowerCase().contains(lower)).count();
        }
    }

    /** StatementInspector를 Hibernate에 주입하는 @TestConfiguration */
    @org.springframework.boot.test.context.TestConfiguration
    static class InspectorConfig {
        static final SqlCapturingInspector INSPECTOR = new SqlCapturingInspector();

        @Bean
        HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
            return props -> props.put("hibernate.session_factory.statement_inspector", INSPECTOR);
        }
    }

    @Autowired
    private EntityManager em;

    @Autowired
    private HouseBlRepository houseBlRepository;

    @Autowired
    private HouseBlAirRepository houseBlAirRepository;

    @Autowired
    private HouseBlTruckRepository houseBlTruckRepository;

    @Autowired
    private HouseBlSeaDescRepository houseBlSeaDescRepository;

    @Autowired
    private HouseBlAirDescRepository houseBlAirDescRepository;

    @Autowired
    private HouseBlTruckDescRepository houseBlTruckDescRepository;

    @Autowired
    private HouseBlNonBlRepository houseBlNonBlRepository;

    @BeforeEach
    void resetInspector() {
        InspectorConfig.INSPECTOR.reset();
    }

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────────────

    private HouseBlJpaEntity newParent(JobDiv jobDiv) {
        HouseBlJpaEntity p = new HouseBlJpaEntity();
        p.setJobDiv(jobDiv);
        p.setBound(Bound.EXP);
        return p;
    }

    private HouseBlSeaContainerJpaEntity seaContainer(String containerNo) {
        HouseBlSeaContainerJpaEntity c = new HouseBlSeaContainerJpaEntity();
        c.setContainerNo(containerNo);
        c.setContainerType(ContainerType.T20GP);
        c.setLengthFeet(20);
        return c;
    }

    private HouseBlNonBlContainerJpaEntity nonBlContainer(String containerNo) {
        HouseBlNonBlContainerJpaEntity c = new HouseBlNonBlContainerJpaEntity();
        c.setContainerNo(containerNo);
        c.setContainerType(ContainerType.T20GP);
        c.setLengthFeet(20);
        return c;
    }

    private HouseBlAirDimJpaEntity airDim() {
        HouseBlAirDimJpaEntity d = new HouseBlAirDimJpaEntity();
        d.setQuantity(1);
        return d;
    }

    private HouseBlTruckDimJpaEntity truckDim() {
        HouseBlTruckDimJpaEntity d = new HouseBlTruckDimJpaEntity();
        d.setQuantity(1);
        return d;
    }

    private HouseBlNonBlDimJpaEntity nonBlDim() {
        HouseBlNonBlDimJpaEntity d = new HouseBlNonBlDimJpaEntity();
        d.setQuantity(1);
        return d;
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

    private HouseBlSeaDescJpaEntity seaDesc(HouseBlSeaJpaEntity seaExt, String marks) {
        HouseBlSeaDescJpaEntity d = new HouseBlSeaDescJpaEntity();
        d.setSea(seaExt);
        d.setMarks(marks);
        return d;
    }

    private HouseBlAirDescJpaEntity airDesc(HouseBlAirJpaEntity airExt, String marks) {
        HouseBlAirDescJpaEntity d = new HouseBlAirDescJpaEntity();
        d.setAir(airExt);
        d.setMarks(marks);
        return d;
    }

    private HouseBlTruckDescJpaEntity truckDesc(HouseBlTruckJpaEntity truckExt, String marks) {
        HouseBlTruckDescJpaEntity d = new HouseBlTruckDescJpaEntity();
        d.setTruck(truckExt);
        d.setMarks(marks);
        return d;
    }

    private HouseBlSeaJpaEntity newSeaExt(HouseBlJpaEntity parent) {
        HouseBlSeaJpaEntity seaJpa = new HouseBlSeaJpaEntity();
        seaJpa.setHouseBl(parent);
        return seaJpa;
    }

    private HouseBlNonBlJpaEntity newNonBlExt(HouseBlJpaEntity parent) {
        HouseBlNonBlJpaEntity nonBlJpa = new HouseBlNonBlJpaEntity();
        nonBlJpa.setHouseBl(parent);
        nonBlJpa.setWorkDivision(HouseBlNonBl.WorkDivision.SEA);
        return nonBlJpa;
    }

    private long countAirDims(Long airExtId) {
        return em.createQuery(
                        "SELECT COUNT(d) FROM HouseBlAirDimJpaEntity d WHERE d.houseBlAirId = :aid", Long.class)
                .setParameter("aid", airExtId)
                .getSingleResult();
    }

    private long countTruckDims(Long truckExtId) {
        return em.createQuery(
                        "SELECT COUNT(d) FROM HouseBlTruckDimJpaEntity d WHERE d.houseBlTruckId = :tid", Long.class)
                .setParameter("tid", truckExtId)
                .getSingleResult();
    }

    private long countNonBlDims(Long nonBlExtId) {
        return em.createQuery(
                        "SELECT COUNT(d) FROM HouseBlNonBlDimJpaEntity d WHERE d.houseBlNonBlId = :nid", Long.class)
                .setParameter("nid", nonBlExtId)
                .getSingleResult();
    }

    /** SEA 컨테이너는 house_bl_sea_container.house_bl_sea_id FK 소유 */
    private long countSeaContainers(Long seaId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM HouseBlSeaContainerJpaEntity c WHERE c.houseBlSeaId = :sid", Long.class)
                .setParameter("sid", seaId)
                .getSingleResult();
    }

    /** NON_BL 컨테이너는 house_bl_nonbl_container.house_bl_non_bl_id FK 소유 */
    private long countNonBlContainers(Long nonBlId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM HouseBlNonBlContainerJpaEntity c WHERE c.houseBlNonBlId = :nid", Long.class)
                .setParameter("nid", nonBlId)
                .getSingleResult();
    }

    /** scheduleLegs는 house_bl_air_id FK로 소유 — airJpa id 기준으로 집계 */
    private long countScheduleLegs(Long airId) {
        return em.createQuery(
                        "SELECT COUNT(l) FROM HouseBlScheduleLegJpaEntity l WHERE l.houseBlAirId = :aid", Long.class)
                .setParameter("aid", airId)
                .getSingleResult();
    }

    /** airCharges는 house_bl_air_id FK로 소유 — airJpa id 기준으로 집계 */
    private long countAirCharges(Long airId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM HouseBlAirChargeJpaEntity c WHERE c.houseBlAirId = :aid", Long.class)
                .setParameter("aid", airId)
                .getSingleResult();
    }

    private HouseBlAirJpaEntity newAirExt(HouseBlJpaEntity parent) {
        HouseBlAirJpaEntity airJpa = new HouseBlAirJpaEntity();
        airJpa.setHouseBl(parent);
        return airJpa;
    }

    private HouseBlTruckJpaEntity newTruckExt(HouseBlJpaEntity parent) {
        HouseBlTruckJpaEntity truckJpa = new HouseBlTruckJpaEntity();
        truckJpa.setHouseBl(parent);
        return truckJpa;
    }

    /** truckOrders는 house_bl_truck_id FK로 소유 — truckExt id 기준으로 집계 */
    private long countTruckOrders(Long truckId) {
        return em.createQuery(
                        "SELECT COUNT(o) FROM HouseBlTruckOrderJpaEntity o WHERE o.houseBlTruckId = :tid", Long.class)
                .setParameter("tid", truckId)
                .getSingleResult();
    }

    // ── 테스트 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Sea 모드 save→load: seaExt에 containers 2건 라운드트립 (SEA는 dim 미사용)")
    void seaMode_fullRoundTrip_allChildrenRestored() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        em.persist(parent);
        em.flush();

        // SEA 컨테이너는 seaExt 소유 — seaExt 영속화 후 sync
        HouseBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();
        seaExt.syncContainers(List.of(seaContainer("CONT001"), seaContainer("CONT002")));
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        HouseBlSeaJpaEntity loadedSea = em.find(HouseBlSeaJpaEntity.class, seaExt.getHouseBlSeaId());

        assertThat(loaded).isNotNull();
        assertThat(loadedSea.getContainers()).hasSize(2);
    }

    @Test
    @DisplayName("Air 모드 save→load: airExt에 dims 1건/scheduleLegs 1건/airCharges 1건 라운드트립")
    void airMode_fullRoundTrip_correctCollections() {
        HouseBlJpaEntity parent = newParent(JobDiv.AIR);
        em.persist(parent);
        em.flush();

        // dims/scheduleLegs/airCharges는 HouseBlAirJpaEntity 소유 — airExt 영속화 후 sync
        HouseBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();
        airExt.syncDims(List.of(airDim()));
        airExt.syncScheduleLegs(List.of(scheduleLeg("USNYC")));
        airExt.syncAirCharges(List.of(airCharge("FUEL")));
        em.flush();
        em.clear();

        HouseBlAirJpaEntity loadedAir = em.find(HouseBlAirJpaEntity.class, airExt.getHouseBlAirId());

        assertThat(loadedAir.getDims()).hasSize(1);
        assertThat(loadedAir.getScheduleLegs()).hasSize(1);
        assertThat(loadedAir.getAirCharges()).hasSize(1);
    }

    @Test
    @DisplayName("Truck 모드 save→load: truckExt에 dims 1건 + truckOrders 1건 라운드트립")
    void truckMode_roundTrip_truckOrdersAndDimsOnly() {
        HouseBlJpaEntity parent = newParent(JobDiv.TRUCK);
        em.persist(parent);
        em.flush();

        // dims/truckOrders는 HouseBlTruckJpaEntity 소유 — truckExt 영속화 후 sync
        HouseBlTruckJpaEntity truckExt = newTruckExt(parent);
        em.persist(truckExt);
        em.flush();
        truckExt.syncDims(List.of(truckDim()));
        truckExt.syncTruckOrders(List.of(truckOrder("TRUCK-1")));
        em.flush();
        em.clear();

        HouseBlTruckJpaEntity loadedTruck = em.find(HouseBlTruckJpaEntity.class, truckExt.getHouseBlTruckId());

        assertThat(loadedTruck.getDims()).hasSize(1);
        assertThat(loadedTruck.getTruckOrders()).hasSize(1);
    }

    @Test
    @DisplayName("NonBl 모드 save→load: nonBlExt에 containers 1건 + dims 1건 라운드트립")
    void nonBlMode_roundTrip_containersAndDims() {
        HouseBlJpaEntity parent = newParent(JobDiv.NON_BL);
        em.persist(parent);
        em.flush();

        // NON_BL 컨테이너/dim은 nonBlExt 소유 — nonBlExt 영속화 후 merge
        HouseBlNonBlJpaEntity nonBlExt = newNonBlExt(parent);
        em.persist(nonBlExt);
        em.flush();
        nonBlExt.mergeContainers(List.of(nonBlContainer("CONT-NON-BL")));
        nonBlExt.mergeDims(List.of(nonBlDim()));
        em.flush();
        em.clear();

        HouseBlNonBlJpaEntity loadedNonBl = em.find(HouseBlNonBlJpaEntity.class, nonBlExt.getHouseBlNonBlId());

        assertThat(loadedNonBl.getContainers()).hasSize(1);
        assertThat(loadedNonBl.getDims()).hasSize(1);
    }

    @Test
    @DisplayName("빈 컬렉션 저장/조회: NPE 없음, seaExt getContainers()는 빈 List 반환, airExt/truckExt getDims()는 빈 List 반환")
    void emptyCollections_noPeNpeAndEmptyListReturned() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        em.persist(parent);
        em.flush();

        HouseBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();

        HouseBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();
        em.clear();

        HouseBlSeaJpaEntity loadedSea = em.find(HouseBlSeaJpaEntity.class, seaExt.getHouseBlSeaId());
        HouseBlAirJpaEntity loadedAir = em.find(HouseBlAirJpaEntity.class, airExt.getHouseBlAirId());

        assertThat(loadedSea.getContainers()).isNotNull().isEmpty();
        assertThat(loadedAir.getDims()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("SEA seaExt.syncContainers: 자식 일부 필드 수정 → flush → 재조회 시 변경 반영, 다른 자식 unchanged")
    void partialChildUpdate_sealNo1_persisted_otherChildUnchanged() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        em.persist(parent);
        em.flush();

        HouseBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();
        seaExt.syncContainers(List.of(seaContainer("CONT-A"), seaContainer("CONT-B")));
        em.flush();
        em.clear();

        HouseBlSeaJpaEntity loadedSea = em.find(HouseBlSeaJpaEntity.class, seaExt.getHouseBlSeaId());
        HouseBlSeaContainerJpaEntity targetContainer = loadedSea.getContainers().stream()
                .filter(c -> "CONT-A".equals(c.getContainerNo()))
                .findFirst().orElseThrow();
        targetContainer.setSealNo1("SEAL-001");

        em.flush();
        em.clear();

        HouseBlSeaJpaEntity reloadedSea = em.find(HouseBlSeaJpaEntity.class, seaExt.getHouseBlSeaId());
        HouseBlSeaContainerJpaEntity updatedA = reloadedSea.getContainers().stream()
                .filter(c -> "CONT-A".equals(c.getContainerNo()))
                .findFirst().orElseThrow();
        HouseBlSeaContainerJpaEntity unchangedB = reloadedSea.getContainers().stream()
                .filter(c -> "CONT-B".equals(c.getContainerNo()))
                .findFirst().orElseThrow();

        assertThat(updatedA.getSealNo1()).isEqualTo("SEAL-001");
        assertThat(unchangedB.getSealNo1()).isNull();
    }

    @Test
    @DisplayName("seaExt.syncContainers(newList): 기존 2건 → 신규 3건 교체. flush 후 자식 row count == 3, 기존 ID 모두 사라짐")
    void seaSyncContainers_replaceTwoWithThree_orphanRemovedAndNewRowsInserted() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        em.persist(parent);
        em.flush();

        HouseBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();
        seaExt.syncContainers(List.of(seaContainer("OLD-1"), seaContainer("OLD-2")));
        em.flush();
        em.clear();

        HouseBlSeaJpaEntity loadedSea = em.find(HouseBlSeaJpaEntity.class, seaExt.getHouseBlSeaId());
        List<Long> oldIds = loadedSea.getContainers().stream()
                .map(HouseBlSeaContainerJpaEntity::getHouseBlSeaContainerId)
                .toList();

        loadedSea.syncContainers(List.of(seaContainer("NEW-1"), seaContainer("NEW-2"), seaContainer("NEW-3")));
        em.flush();
        em.clear();

        long count = countSeaContainers(seaExt.getHouseBlSeaId());
        assertThat(count).isEqualTo(3);

        HouseBlSeaJpaEntity reloadedSea = em.find(HouseBlSeaJpaEntity.class, seaExt.getHouseBlSeaId());
        List<Long> newIds = reloadedSea.getContainers().stream()
                .map(HouseBlSeaContainerJpaEntity::getHouseBlSeaContainerId)
                .toList();

        assertThat(newIds).doesNotContainAnyElementsOf(oldIds);
        assertThat(reloadedSea.getContainers())
                .extracting(HouseBlSeaContainerJpaEntity::getContainerNo)
                .containsExactlyInAnyOrder("NEW-1", "NEW-2", "NEW-3");
    }

    @Test
    @DisplayName("seaExt.syncContainers(emptyList): 기존 모든 자식 DELETE — DB row count == 0")
    void seaSyncContainers_emptyList_allChildrenDeleted() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        em.persist(parent);
        em.flush();

        HouseBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();
        seaExt.syncContainers(List.of(seaContainer("CONT-X"), seaContainer("CONT-Y")));
        em.flush();
        em.clear();

        HouseBlSeaJpaEntity loadedSea = em.find(HouseBlSeaJpaEntity.class, seaExt.getHouseBlSeaId());
        loadedSea.syncContainers(List.of());
        em.flush();
        em.clear();

        assertThat(countSeaContainers(seaExt.getHouseBlSeaId())).isZero();
    }

    @Test
    @DisplayName("nonBlExt.mergeContainers: 신규 1건 저장 후 조회 → row 1건 확인")
    void nonBlMergeContainers_newContainer_isSaved() {
        HouseBlJpaEntity parent = newParent(JobDiv.NON_BL);
        em.persist(parent);
        em.flush();

        HouseBlNonBlJpaEntity nonBlExt = newNonBlExt(parent);
        em.persist(nonBlExt);
        em.flush();
        nonBlExt.mergeContainers(List.of(nonBlContainer("NONBL-001")));
        em.flush();
        em.clear();

        assertThat(countNonBlContainers(nonBlExt.getHouseBlNonBlId())).isEqualTo(1);
    }

    @Test
    @DisplayName("nonBlExt.mergeContainers(emptyList): 기존 모든 자식 DELETE — DB row count == 0")
    void nonBlMergeContainers_emptyList_allChildrenDeleted() {
        HouseBlJpaEntity parent = newParent(JobDiv.NON_BL);
        em.persist(parent);
        em.flush();

        HouseBlNonBlJpaEntity nonBlExt = newNonBlExt(parent);
        em.persist(nonBlExt);
        em.flush();
        nonBlExt.mergeContainers(List.of(nonBlContainer("NONBL-DEL")));
        em.flush();
        em.clear();

        HouseBlNonBlJpaEntity loadedNonBl = em.find(HouseBlNonBlJpaEntity.class, nonBlExt.getHouseBlNonBlId());
        loadedNonBl.mergeContainers(List.of());
        em.flush();
        em.clear();

        assertThat(countNonBlContainers(nonBlExt.getHouseBlNonBlId())).isZero();
    }

    @Test
    @DisplayName("seaExt 삭제 → SEA container CASCADE 삭제 확인. truckExt/airExt도 각자 자식(dim 포함) cascade 정리")
    void parentDelete_cascadeDeleteAllChildren() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        em.persist(parent);
        em.flush();

        // SEA 컨테이너는 seaExt 소유
        HouseBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();
        seaExt.syncContainers(List.of(seaContainer("CONT-DEL")));
        em.flush();

        // dims/truckOrders는 truckExt 소유
        HouseBlTruckJpaEntity truckExt = newTruckExt(parent);
        em.persist(truckExt);
        em.flush();
        truckExt.syncDims(List.of(truckDim()));
        truckExt.syncTruckOrders(List.of(truckOrder("TRUCK-DEL")));
        em.flush();

        // dims/scheduleLegs/airCharges는 airExt 소유
        HouseBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();
        airExt.syncDims(List.of(airDim()));
        airExt.syncScheduleLegs(List.of(scheduleLeg("KRPUS")));
        airExt.syncAirCharges(List.of(airCharge("FUEL-DEL")));
        em.flush();
        em.clear();

        Long parentId = parent.getHouseBlId();
        Long seaId = seaExt.getHouseBlSeaId();
        Long truckId = truckExt.getHouseBlTruckId();
        Long airId = airExt.getHouseBlAirId();

        // seaExt 제거 — seaContainer ON DELETE CASCADE로 자동 정리
        HouseBlSeaJpaEntity seaToDelete = em.find(HouseBlSeaJpaEntity.class, seaId);
        em.remove(seaToDelete);
        em.flush();
        // truckExt 제거 — truckDim/truckOrders cascade 삭제
        HouseBlTruckJpaEntity truckToDelete = em.find(HouseBlTruckJpaEntity.class, truckId);
        em.remove(truckToDelete);
        em.flush();
        // airExt 제거 — airDim/scheduleLegs/airCharges cascade 삭제
        HouseBlAirJpaEntity airToDelete = em.find(HouseBlAirJpaEntity.class, airId);
        em.remove(airToDelete);
        em.flush();
        HouseBlJpaEntity toDelete = em.find(HouseBlJpaEntity.class, parentId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        assertThat(em.find(HouseBlJpaEntity.class, parentId)).isNull();
        assertThat(countSeaContainers(seaId)).isZero();
        assertThat(countTruckDims(truckId)).isZero();
        assertThat(countTruckOrders(truckId)).isZero();
        assertThat(countAirDims(airId)).isZero();
        assertThat(countScheduleLegs(airId)).isZero();
        assertThat(countAirCharges(airId)).isZero();
    }

    @Test
    @DisplayName("seaDescReplace: SEA ext로 기존 seaDesc 삭제 후 새 seaDesc 저장 → 기존 descId로 조회 시 null, 새 desc marks 일치")
    void replaceSeaDesc_orphanDescIsDeletedFromDb() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        em.persist(parent);
        em.flush();

        // seaExt 영속화 (house_bl_sea_id PK 확보)
        HouseBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();

        // 초기 seaDesc 저장
        HouseBlSeaDescJpaEntity oldDesc = seaDesc(seaExt, "OLD MARKS");
        em.persist(oldDesc);
        em.flush();
        em.clear();

        Long oldDescId = houseBlSeaDescRepository.findBySea_HouseBlSeaId(seaExt.getHouseBlSeaId())
                .orElseThrow().getHouseBlSeaDescId();

        // 1단계: 기존 desc DELETE
        houseBlSeaDescRepository.deleteBySea_HouseBlSeaId(seaExt.getHouseBlSeaId());
        em.flush();

        // 2단계: 새 desc INSERT
        HouseBlSeaJpaEntity seaRef = em.find(HouseBlSeaJpaEntity.class, seaExt.getHouseBlSeaId());
        HouseBlSeaDescJpaEntity newDesc = seaDesc(seaRef, "NEW MARKS");
        em.persist(newDesc);
        em.flush();
        em.clear();

        assertThat(em.find(HouseBlSeaDescJpaEntity.class, oldDescId)).isNull();
        HouseBlSeaDescJpaEntity reloaded = houseBlSeaDescRepository.findBySea_HouseBlSeaId(seaExt.getHouseBlSeaId()).orElseThrow();
        assertThat(reloaded.getMarks()).isEqualTo("NEW MARKS");
    }

    @Test
    @DisplayName("airDescReplace: AIR ext로 기존 airDesc 삭제 후 새 airDesc 저장 → 기존 descId로 조회 시 null, 새 desc marks 일치")
    void replaceAirDesc_orphanDescIsDeletedFromDb() {
        HouseBlJpaEntity parent = newParent(JobDiv.AIR);
        em.persist(parent);
        em.flush();

        HouseBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();

        HouseBlAirDescJpaEntity oldDesc = airDesc(airExt, "OLD AIR MARKS");
        em.persist(oldDesc);
        em.flush();
        em.clear();

        Long oldDescId = houseBlAirDescRepository.findByAir_HouseBlAirId(airExt.getHouseBlAirId())
                .orElseThrow().getHouseBlAirDescId();

        houseBlAirDescRepository.deleteByAir_HouseBlAirId(airExt.getHouseBlAirId());
        em.flush();

        HouseBlAirJpaEntity airRef = em.find(HouseBlAirJpaEntity.class, airExt.getHouseBlAirId());
        HouseBlAirDescJpaEntity newDesc = airDesc(airRef, "NEW AIR MARKS");
        em.persist(newDesc);
        em.flush();
        em.clear();

        assertThat(em.find(HouseBlAirDescJpaEntity.class, oldDescId)).isNull();
        HouseBlAirDescJpaEntity reloaded = houseBlAirDescRepository.findByAir_HouseBlAirId(airExt.getHouseBlAirId()).orElseThrow();
        assertThat(reloaded.getMarks()).isEqualTo("NEW AIR MARKS");
    }

    @Test
    @DisplayName("airExt.syncDims: 2건 저장 후 3건으로 교체 → flush → DB count==3, 기존 ID 없음")
    void syncDims_replace_orphanRemovedAndNewRowsInserted() {
        HouseBlJpaEntity parent = newParent(JobDiv.AIR);
        em.persist(parent);
        em.flush();

        // airExt 영속화 후 dim sync
        HouseBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();
        airExt.syncDims(List.of(airDim(), airDim()));
        em.flush();
        em.clear();

        HouseBlAirJpaEntity loadedAir = em.find(HouseBlAirJpaEntity.class, airExt.getHouseBlAirId());
        List<Long> oldIds = loadedAir.getDims().stream()
                .map(HouseBlAirDimJpaEntity::getHouseBlAirDimId)
                .toList();

        loadedAir.syncDims(List.of(airDim(), airDim(), airDim()));
        em.flush();
        em.clear();

        long count = countAirDims(airExt.getHouseBlAirId());
        assertThat(count).isEqualTo(3);

        HouseBlAirJpaEntity reloadedAir = em.find(HouseBlAirJpaEntity.class, airExt.getHouseBlAirId());
        List<Long> newIds = reloadedAir.getDims().stream()
                .map(HouseBlAirDimJpaEntity::getHouseBlAirDimId)
                .toList();
        assertThat(newIds).doesNotContainAnyElementsOf(oldIds);
    }

    @Test
    @DisplayName("HouseBlAirJpaEntity.syncScheduleLegs: 1건 저장 후 empty 전달 → flush → DB count==0")
    void syncScheduleLegs_emptyList_allDeletedFromDb() {
        HouseBlJpaEntity parent = newParent(JobDiv.AIR);
        em.persist(parent);
        em.flush();

        HouseBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();
        airExt.syncScheduleLegs(List.of(scheduleLeg("USNYC")));
        em.flush();
        em.clear();

        HouseBlAirJpaEntity loadedAir = em.find(HouseBlAirJpaEntity.class, airExt.getHouseBlAirId());
        loadedAir.syncScheduleLegs(List.of());
        em.flush();
        em.clear();

        assertThat(countScheduleLegs(airExt.getHouseBlAirId())).isZero();
    }

    @Test
    @DisplayName("HouseBlTruckJpaEntity.syncTruckOrders: 1건 저장 후 empty 전달 → flush → DB count==0")
    void syncTruckOrders_emptyList_allDeletedFromDb() {
        HouseBlJpaEntity parent = newParent(JobDiv.TRUCK);
        em.persist(parent);
        em.flush();

        HouseBlTruckJpaEntity truckExt = newTruckExt(parent);
        em.persist(truckExt);
        em.flush();
        truckExt.syncTruckOrders(List.of(truckOrder("TRUCK-01")));
        em.flush();
        em.clear();

        HouseBlTruckJpaEntity loadedTruck = em.find(HouseBlTruckJpaEntity.class, truckExt.getHouseBlTruckId());
        loadedTruck.syncTruckOrders(List.of());
        em.flush();
        em.clear();

        assertThat(countTruckOrders(truckExt.getHouseBlTruckId())).isZero();
    }

    @Test
    @DisplayName("HouseBlAirJpaEntity.syncAirCharges: 1건 저장 후 다른 1건으로 교체 → DB count==1, 기존 ID 없음")
    void syncAirCharges_replaceOne_orphanRemovedAndNewInserted() {
        HouseBlJpaEntity parent = newParent(JobDiv.AIR);
        em.persist(parent);
        em.flush();

        HouseBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();
        airExt.syncAirCharges(List.of(airCharge("FUEL")));
        em.flush();
        em.clear();

        HouseBlAirJpaEntity loadedAir = em.find(HouseBlAirJpaEntity.class, airExt.getHouseBlAirId());
        List<Long> oldIds = loadedAir.getAirCharges().stream()
                .map(HouseBlAirChargeJpaEntity::getHouseBlAirChargeId)
                .toList();

        loadedAir.syncAirCharges(List.of(airCharge("AWC")));
        em.flush();
        em.clear();

        long count = countAirCharges(airExt.getHouseBlAirId());
        assertThat(count).isEqualTo(1);

        HouseBlAirJpaEntity reloadedAir = em.find(HouseBlAirJpaEntity.class, airExt.getHouseBlAirId());
        List<Long> newIds = reloadedAir.getAirCharges().stream()
                .map(HouseBlAirChargeJpaEntity::getHouseBlAirChargeId)
                .toList();
        assertThat(newIds).doesNotContainAnyElementsOf(oldIds);
    }

    // ── StatementInspector 기반 desc fetch 회귀 테스트 ─────────────────

    @Test
    @DisplayName("nonBlFind_doesNotEmitSeaAirDescSelect: NON_BL 조회 시 house_bl_sea_desc/house_bl_air_desc 테이블 참조 SQL 0건")
    void nonBlFind_doesNotEmitSeaAirDescSelect() {
        HouseBlJpaEntity parent = newParent(JobDiv.NON_BL);
        em.persist(parent);
        em.flush();
        em.clear();
        InspectorConfig.INSPECTOR.reset();

        houseBlRepository.findById(parent.getHouseBlId());

        assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_sea_desc")).isZero();
        assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_air_desc")).isZero();
    }

    @Test
    @DisplayName("truckFind_doesNotEmitSeaAirDescSelect: TRUCK 조회 시 house_bl_sea_desc/house_bl_air_desc 테이블 참조 SQL 0건")
    void truckFind_doesNotEmitSeaAirDescSelect() {
        HouseBlJpaEntity parent = newParent(JobDiv.TRUCK);
        em.persist(parent);
        em.flush();
        em.clear();
        InspectorConfig.INSPECTOR.reset();

        houseBlRepository.findById(parent.getHouseBlId());

        assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_sea_desc")).isZero();
        assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_air_desc")).isZero();
    }

    @Test
    @DisplayName("seaFind_emitsHouseBlSeaDescSelect: SEA seaDescRepository 조회 시 house_bl_sea_desc SELECT 발생, marks 일치")
    void seaFind_emitsHouseBlSeaDescSelect() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        em.persist(parent);
        em.flush();

        HouseBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();

        HouseBlSeaDescJpaEntity seaDescJpa = seaDesc(seaExt, "MARKS-X");
        em.persist(seaDescJpa);
        em.flush();
        em.clear();
        InspectorConfig.INSPECTOR.reset();

        Optional<HouseBlSeaDescJpaEntity> loaded = houseBlSeaDescRepository.findBySea_HouseBlSeaId(seaExt.getHouseBlSeaId());

        assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_sea_desc")).isGreaterThanOrEqualTo(1);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getMarks()).isEqualTo("MARKS-X");
    }

    @Test
    @DisplayName("airFind_emitsHouseBlAirDescSelect: AIR airDescRepository 조회 시 house_bl_air_desc SELECT 발생, marks 일치")
    void airFind_emitsHouseBlAirDescSelect() {
        HouseBlJpaEntity parent = newParent(JobDiv.AIR);
        em.persist(parent);
        em.flush();

        HouseBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();

        HouseBlAirDescJpaEntity airDescJpa = airDesc(airExt, "AIR-MARKS");
        em.persist(airDescJpa);
        em.flush();
        em.clear();
        InspectorConfig.INSPECTOR.reset();

        Optional<HouseBlAirDescJpaEntity> loaded = houseBlAirDescRepository.findByAir_HouseBlAirId(airExt.getHouseBlAirId());

        assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_air_desc")).isGreaterThanOrEqualTo(1);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getMarks()).isEqualTo("AIR-MARKS");
    }

    @Test
    @DisplayName("seaSave_persistsSeaDesc: SEA seaExt + seaDesc persist 후 house_bl_sea_desc row 1건, marks 일치")
    void seaSave_persistsSeaDesc() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        em.persist(parent);
        em.flush();

        HouseBlSeaJpaEntity seaExt = newSeaExt(parent);
        em.persist(seaExt);
        em.flush();

        HouseBlSeaDescJpaEntity seaDescJpa = seaDesc(seaExt, "CASCADE-MARKS");
        em.persist(seaDescJpa);
        em.flush();
        em.clear();

        Long count = em.createQuery(
                "SELECT COUNT(d) FROM HouseBlSeaDescJpaEntity d WHERE d.sea.houseBlSeaId = :id", Long.class)
                .setParameter("id", seaExt.getHouseBlSeaId())
                .getSingleResult();
        assertThat(count).isEqualTo(1);

        HouseBlSeaDescJpaEntity reloaded = houseBlSeaDescRepository.findBySea_HouseBlSeaId(seaExt.getHouseBlSeaId()).orElseThrow();
        assertThat(reloaded.getMarks()).isEqualTo("CASCADE-MARKS");
    }

    @Test
    @DisplayName("nonBlSave_doesNotInsertSeaOrAirDesc: NON_BL parent 저장 후 sea_desc/air_desc row 0건")
    void nonBlSave_doesNotInsertSeaOrAirDesc() {
        HouseBlJpaEntity parent = newParent(JobDiv.NON_BL);
        em.persist(parent);
        em.flush();
        em.clear();

        long seaDescCount = em.createQuery(
                "SELECT COUNT(d) FROM HouseBlSeaDescJpaEntity d", Long.class)
                .getSingleResult();
        long airDescCount = em.createQuery(
                "SELECT COUNT(d) FROM HouseBlAirDescJpaEntity d", Long.class)
                .getSingleResult();
        assertThat(seaDescCount).isZero();
        assertThat(airDescCount).isZero();
    }

    @Test
    @DisplayName("truckDescSave: TRUCK truckExt + truckDesc persist 후 house_bl_truck_desc row 1건, marks 일치")
    void truckDescSave_persistsTruckDesc() {
        HouseBlJpaEntity parent = newParent(JobDiv.TRUCK);
        em.persist(parent);
        em.flush();

        HouseBlTruckJpaEntity truckExt = newTruckExt(parent);
        em.persist(truckExt);
        em.flush();

        HouseBlTruckDescJpaEntity truckDescJpa = truckDesc(truckExt, "TRUCK-CASCADE-MARKS");
        em.persist(truckDescJpa);
        em.flush();
        em.clear();

        Long count = em.createQuery(
                "SELECT COUNT(d) FROM HouseBlTruckDescJpaEntity d WHERE d.truck.houseBlTruckId = :id", Long.class)
                .setParameter("id", truckExt.getHouseBlTruckId())
                .getSingleResult();
        assertThat(count).isEqualTo(1);

        HouseBlTruckDescJpaEntity reloaded = houseBlTruckDescRepository.findByTruck_HouseBlTruckId(truckExt.getHouseBlTruckId()).orElseThrow();
        assertThat(reloaded.getMarks()).isEqualTo("TRUCK-CASCADE-MARKS");
    }

    @Test
    @DisplayName("truckDescFind: truckDescRepository 조회 시 house_bl_truck_desc SELECT 발생, marks 일치")
    void truckDescFind_emitsHouseBlTruckDescSelect() {
        HouseBlJpaEntity parent = newParent(JobDiv.TRUCK);
        em.persist(parent);
        em.flush();

        HouseBlTruckJpaEntity truckExt = newTruckExt(parent);
        em.persist(truckExt);
        em.flush();

        HouseBlTruckDescJpaEntity truckDescJpa = truckDesc(truckExt, "TRUCK-MARKS-X");
        em.persist(truckDescJpa);
        em.flush();
        em.clear();
        InspectorConfig.INSPECTOR.reset();

        Optional<HouseBlTruckDescJpaEntity> loaded = houseBlTruckDescRepository.findByTruck_HouseBlTruckId(truckExt.getHouseBlTruckId());

        assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_truck_desc")).isGreaterThanOrEqualTo(1);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getMarks()).isEqualTo("TRUCK-MARKS-X");
    }

    @Test
    @DisplayName("truckDescReplace: TRUCK ext로 기존 truckDesc 삭제 후 새 truckDesc 저장 → 기존 descId로 조회 시 null, 새 desc marks 일치")
    void replaceTruckDesc_orphanDescIsDeletedFromDb() {
        HouseBlJpaEntity parent = newParent(JobDiv.TRUCK);
        em.persist(parent);
        em.flush();

        HouseBlTruckJpaEntity truckExt = newTruckExt(parent);
        em.persist(truckExt);
        em.flush();

        HouseBlTruckDescJpaEntity oldDesc = truckDesc(truckExt, "OLD TRUCK MARKS");
        em.persist(oldDesc);
        em.flush();
        em.clear();

        Long oldDescId = houseBlTruckDescRepository.findByTruck_HouseBlTruckId(truckExt.getHouseBlTruckId())
                .orElseThrow().getHouseBlTruckDescId();

        houseBlTruckDescRepository.deleteByTruck_HouseBlTruckId(truckExt.getHouseBlTruckId());
        em.flush();

        HouseBlTruckJpaEntity truckRef = em.find(HouseBlTruckJpaEntity.class, truckExt.getHouseBlTruckId());
        HouseBlTruckDescJpaEntity newDesc = truckDesc(truckRef, "NEW TRUCK MARKS");
        em.persist(newDesc);
        em.flush();
        em.clear();

        assertThat(em.find(HouseBlTruckDescJpaEntity.class, oldDescId)).isNull();
        HouseBlTruckDescJpaEntity reloaded = houseBlTruckDescRepository.findByTruck_HouseBlTruckId(truckExt.getHouseBlTruckId()).orElseThrow();
        assertThat(reloaded.getMarks()).isEqualTo("NEW TRUCK MARKS");
    }

    @Test
    @DisplayName("truckSave_doesNotEmitSeaAirDescSelect: TRUCK 저장 후 조회 시 house_bl_sea_desc/house_bl_air_desc 테이블 참조 SQL 0건")
    void truckFind_doesNotEmitSeaAirDescSelect_withTruckDesc() {
        HouseBlJpaEntity parent = newParent(JobDiv.TRUCK);
        em.persist(parent);
        em.flush();
        em.clear();
        InspectorConfig.INSPECTOR.reset();

        houseBlRepository.findById(parent.getHouseBlId());

        assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_sea_desc")).isZero();
        assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_air_desc")).isZero();
    }
}
