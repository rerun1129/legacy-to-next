package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirChargeJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlScheduleLegJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckOrderJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
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
 * StatementInspector 기반 SQL 캡처 회귀 테스트가 포함된다.
 * SqlCapturingInspector 빈을 @TestConfiguration으로 Hibernate에 주입한다.
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
    private HouseBlDescRepository houseBlDescRepository;

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

    private HouseBlContainerJpaEntity container(String containerNo) {
        return HouseBlContainerJpaEntity.of(containerNo, ContainerType.T20GP, 20);
    }

    private HouseBlDimJpaEntity dim() {
        HouseBlDimJpaEntity d = new HouseBlDimJpaEntity();
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

    /** scheduleLegs는 house_bl_air_id FK로 소유 — airJpa id 기준으로 집계 */
    private long countScheduleLegs(Long airId) {
        return em.createQuery(
                        "SELECT COUNT(l) FROM HouseBlScheduleLegJpaEntity l WHERE l.houseBlAirId = :aid", Long.class)
                .setParameter("aid", airId)
                .getSingleResult();
    }

    private HouseBlAirJpaEntity newAirExt(HouseBlJpaEntity parent) {
        HouseBlAirJpaEntity airJpa = new HouseBlAirJpaEntity();
        airJpa.setHouseBl(parent);
        return airJpa;
    }

    // ── 테스트 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Sea 모드 save→load: containers 2건 + dims 1건 라운드트립")
    void seaMode_fullRoundTrip_allChildrenRestored() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        parent.syncContainers(List.of(container("CONT001"), container("CONT002")));
        parent.syncDims(List.of(dim()));

        em.persist(parent);
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getContainers()).hasSize(2);
        assertThat(loaded.getDims()).hasSize(1);
        assertThat(loaded.getTruckOrders()).isEmpty();
        assertThat(loaded.getAirCharges()).isEmpty();
    }

    @Test
    @DisplayName("Air 모드 save→load: dims/scheduleLegs/airCharges 채움, containers/truckOrders 빈 목록")
    void airMode_fullRoundTrip_correctCollections() {
        HouseBlJpaEntity parent = newParent(JobDiv.AIR);
        parent.syncDims(List.of(dim()));
        parent.syncAirCharges(List.of(airCharge("FUEL")));
        em.persist(parent);
        em.flush();

        // scheduleLegs는 HouseBlAirJpaEntity 소유 — airExt 영속화 후 sync
        HouseBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();
        airExt.syncScheduleLegs(List.of(scheduleLeg("USNYC")));
        em.flush();
        em.clear();

        HouseBlJpaEntity loaded = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        HouseBlAirJpaEntity loadedAir = em.find(HouseBlAirJpaEntity.class, airExt.getHouseBlAirId());

        assertThat(loaded.getContainers()).isEmpty();
        assertThat(loaded.getTruckOrders()).isEmpty();
        assertThat(loaded.getDims()).hasSize(1);
        assertThat(loadedAir.getScheduleLegs()).hasSize(1);
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
    @DisplayName("부모 delete → Container/Dim/TruckOrder/AirCharge 자식 row count 모두 0 (cascade)")
    void parentDelete_cascadeDeleteAllChildren() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        parent.syncContainers(List.of(container("CONT-DEL")));
        parent.syncDims(List.of(dim()));
        parent.syncTruckOrders(List.of(truckOrder("TRUCK-DEL")));
        parent.syncAirCharges(List.of(airCharge("FUEL-DEL")));
        em.persist(parent);
        em.flush();

        // scheduleLegs는 AIR ext 소유 — ON DELETE CASCADE로 airExt 삭제 시 자동 정리
        HouseBlAirJpaEntity airExt = newAirExt(parent);
        em.persist(airExt);
        em.flush();
        airExt.syncScheduleLegs(List.of(scheduleLeg("KRPUS")));
        em.flush();
        em.clear();

        Long parentId = parent.getHouseBlId();
        Long airId = airExt.getHouseBlAirId();
        HouseBlJpaEntity toDelete = em.find(HouseBlJpaEntity.class, parentId);
        // airExt도 먼저 제거 (house_bl FK 참조 해제)
        HouseBlAirJpaEntity airToDelete = em.find(HouseBlAirJpaEntity.class, airId);
        em.remove(airToDelete);
        em.flush();
        em.remove(toDelete);
        em.flush();
        em.clear();

        assertThat(em.find(HouseBlJpaEntity.class, parentId)).isNull();
        assertThat(countChildren("HouseBlContainerJpaEntity", parentId)).isZero();
        assertThat(countChildren("HouseBlDimJpaEntity", parentId)).isZero();
        assertThat(countScheduleLegs(airId)).isZero();
        assertThat(countChildren("HouseBlTruckOrderJpaEntity", parentId)).isZero();
        assertThat(countChildren("HouseBlAirChargeJpaEntity", parentId)).isZero();
    }

    @Test
    @DisplayName("descReplace: 기존 desc 삭제 후 새 desc 저장 → 기존 descId로 조회 시 null, 새 desc marks 일치")
    void replaceDesc_orphanDescIsDeletedFromDb() {
        HouseBlJpaEntity parent = newParent(JobDiv.SEA);
        em.persist(parent);
        em.flush();

        // 초기 desc 저장 (자식 owning side persist)
        HouseBlDescJpaEntity oldDesc = desc(parent, "OLD MARKS");
        em.persist(oldDesc);
        em.flush();
        em.clear();

        Long oldDescId = houseBlDescRepository.findByHouseBl_HouseBlId(parent.getHouseBlId())
                .orElseThrow().getHouseBlDescId();

        // 1단계: 기존 desc DELETE (어댑터 saveOrDeleteDesc의 null-desc 경로 흉내)
        houseBlDescRepository.deleteByHouseBl_HouseBlId(parent.getHouseBlId());
        em.flush();

        // 2단계: 새 desc INSERT (어댑터 saveOrDeleteDesc의 신규 save 경로 흉내)
        HouseBlJpaEntity parentRef = em.find(HouseBlJpaEntity.class, parent.getHouseBlId());
        HouseBlDescJpaEntity newDesc = desc(parentRef, "NEW MARKS");
        em.persist(newDesc);
        em.flush();
        em.clear();

        assertThat(em.find(HouseBlDescJpaEntity.class, oldDescId)).isNull();
        HouseBlDescJpaEntity reloaded = houseBlDescRepository.findByHouseBl_HouseBlId(parent.getHouseBlId()).orElseThrow();
        assertThat(reloaded.getMarks()).isEqualTo("NEW MARKS");
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

    // ── StatementInspector 기반 desc fetch 회귀 테스트 ─────────────────

    @Test
    @DisplayName("nonBlFind_doesNotEmitHouseBlDescSelect: NON_BL 조회 시 house_bl_desc 테이블 참조 SQL 0건")
        void nonBlFind_doesNotEmitHouseBlDescSelect() {
            HouseBlJpaEntity parent = newParent(JobDiv.NON_BL);
            em.persist(parent);
            em.flush();
            em.clear();
            InspectorConfig.INSPECTOR.reset();

            // NON_BL은 findById만 호출하므로 desc JOIN 없음
            houseBlRepository.findById(parent.getHouseBlId());

            assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_desc")).isZero();
        }

        @Test
        @DisplayName("truckFind_doesNotEmitHouseBlDescSelect: TRUCK 조회 시 house_bl_desc 테이블 참조 SQL 0건")
        void truckFind_doesNotEmitHouseBlDescSelect() {
            HouseBlJpaEntity parent = newParent(JobDiv.TRUCK);
            em.persist(parent);
            em.flush();
            em.clear();
            InspectorConfig.INSPECTOR.reset();

            houseBlRepository.findById(parent.getHouseBlId());

            assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_desc")).isZero();
        }

        @Test
        @DisplayName("seaFind_emitsHouseBlDescSelect: SEA houseBlDescRepository 조회 시 house_bl_desc SELECT 발생, marks 일치")
        void seaFind_emitsHouseBlDescJoin() {
            HouseBlJpaEntity parent = newParent(JobDiv.SEA);
            em.persist(parent);
            em.flush();

            // 자식 owning side만 persist — 부모 매핑 없음
            HouseBlDescJpaEntity seaDesc = desc(parent, "MARKS-X");
            em.persist(seaDesc);
            em.flush();
            em.clear();
            InspectorConfig.INSPECTOR.reset();

            Optional<HouseBlDescJpaEntity> loaded = houseBlDescRepository.findByHouseBl_HouseBlId(parent.getHouseBlId());

            assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_desc")).isGreaterThanOrEqualTo(1);
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getMarks()).isEqualTo("MARKS-X");
        }

        @Test
        @DisplayName("airFind_emitsHouseBlDescSelect: AIR houseBlDescRepository 조회 시 house_bl_desc SELECT 발생, marks 일치")
        void airFind_emitsHouseBlDescJoin() {
            HouseBlJpaEntity parent = newParent(JobDiv.AIR);
            em.persist(parent);
            em.flush();

            // 자식 owning side만 persist — 부모 매핑 없음
            HouseBlDescJpaEntity airDesc = desc(parent, "AIR-MARKS");
            em.persist(airDesc);
            em.flush();
            em.clear();
            InspectorConfig.INSPECTOR.reset();

            Optional<HouseBlDescJpaEntity> loaded = houseBlDescRepository.findByHouseBl_HouseBlId(parent.getHouseBlId());

            assertThat(InspectorConfig.INSPECTOR.countContaining("house_bl_desc")).isGreaterThanOrEqualTo(1);
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getMarks()).isEqualTo("AIR-MARKS");
        }

        @Test
        @DisplayName("seaSave_persistsDesc: SEA parent + desc persist 후 house_bl_desc row 1건, marks 일치")
        void seaSave_cascadesDesc() {
            HouseBlJpaEntity parent = newParent(JobDiv.SEA);
            em.persist(parent);
            em.flush();

            // 자식 owning side만 persist — 부모 매핑 없음
            HouseBlDescJpaEntity seaDesc = desc(parent, "CASCADE-MARKS");
            em.persist(seaDesc);
            em.flush();
            em.clear();

            Long count = em.createQuery(
                    "SELECT COUNT(d) FROM HouseBlDescJpaEntity d WHERE d.houseBl.houseBlId = :id", Long.class)
                    .setParameter("id", parent.getHouseBlId())
                    .getSingleResult();
            assertThat(count).isEqualTo(1);

            HouseBlDescJpaEntity reloaded = houseBlDescRepository.findByHouseBl_HouseBlId(parent.getHouseBlId()).orElseThrow();
            assertThat(reloaded.getMarks()).isEqualTo("CASCADE-MARKS");
        }

        @Test
        @DisplayName("nonBlSave_doesNotInsertHouseBlDesc: NON_BL parent 저장 후 house_bl_desc row 0건")
        void nonBlSave_doesNotInsertHouseBlDesc() {
            HouseBlJpaEntity parent = newParent(JobDiv.NON_BL);
            em.persist(parent);
            em.flush();
            em.clear();

            Long count = em.createQuery(
                    "SELECT COUNT(d) FROM HouseBlDescJpaEntity d WHERE d.houseBl.houseBlId = :id", Long.class)
                    .setParameter("id", parent.getHouseBlId())
                    .getSingleResult();
            assertThat(count).isZero();
        }
}
