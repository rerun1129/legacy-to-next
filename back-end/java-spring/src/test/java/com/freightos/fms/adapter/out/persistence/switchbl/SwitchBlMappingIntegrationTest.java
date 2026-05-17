package com.freightos.fms.adapter.out.persistence.switchbl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlDescriptionJpaEntity;
import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlJpaEntity;
import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Switch B/L JPA 매핑의 저장·라운드트립·cascade 동작을 검증한다.
 * @DataJpaTest 슬라이스 + H2 in-memory(application-test.yml).
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class SwitchBlMappingIntegrationTest {

    @Autowired
    private EntityManager em;

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────────────

    private HouseBlJpaEntity newHouseBl() {
        HouseBlJpaEntity p = new HouseBlJpaEntity();
        p.setJobDiv(JobDiv.SEA);
        p.setBound(Bound.EXP);
        p.setShipperCode("SHIPPER01");
        return p;
    }

    private SwitchBlJpaEntity newSwitchBl(HouseBlJpaEntity houseBl) {
        SwitchBlJpaEntity s = new SwitchBlJpaEntity();
        s.setHouseBl(houseBl);
        s.setSwitchBlNo("SBL-TEST");
        s.setShipperCode("SWITCH-SHIPPER");
        return s;
    }

    private SwitchBlDescriptionJpaEntity newDescription(SwitchBlJpaEntity switchBl,
                                                         String marks, String natureQuantity) {
        SwitchBlDescriptionJpaEntity d = new SwitchBlDescriptionJpaEntity();
        d.setSwitchBl(switchBl);
        d.setMarks(marks);
        d.setNatureQuantity(natureQuantity);
        return d;
    }

    // ── 테스트 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("SwitchBl 저장 후 flush+clear → 재조회 시 houseBl 링크가 유지된다")
    void saveSwitchBl_persistsWithHouseBlLink() {
        HouseBlJpaEntity houseBl = newHouseBl();
        em.persist(houseBl);
        em.flush();

        SwitchBlJpaEntity switchBl = newSwitchBl(houseBl);
        em.persist(switchBl);
        em.flush();
        em.clear();

        SwitchBlJpaEntity loaded = em.find(SwitchBlJpaEntity.class, switchBl.getSwitchBlId());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getHouseBl().getHouseBlId()).isEqualTo(houseBl.getHouseBlId());
    }

    @Test
    @DisplayName("SwitchBl + description 저장 후 flush+clear → 재조회 시 description 필드 값이 유지된다")
    void saveSwitchBl_withDescription_roundTrip() {
        HouseBlJpaEntity houseBl = newHouseBl();
        em.persist(houseBl);
        em.flush();

        SwitchBlJpaEntity switchBl = newSwitchBl(houseBl);
        em.persist(switchBl);
        em.flush();

        SwitchBlDescriptionJpaEntity desc = newDescription(switchBl, "MARKS-VALUE", "NATURE-VALUE");
        em.persist(desc);
        em.flush();
        em.clear();

        SwitchBlDescriptionJpaEntity loaded = em.find(
                SwitchBlDescriptionJpaEntity.class, desc.getSwitchBlDescriptionId());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getMarks()).isEqualTo("MARKS-VALUE");
        assertThat(loaded.getNatureQuantity()).isEqualTo("NATURE-VALUE");
        assertThat(loaded.getSwitchBl().getSwitchBlId()).isEqualTo(switchBl.getSwitchBlId());
    }

    @Test
    @DisplayName("SwitchBl description null 저장 → 재조회 시 description 필드가 null")
    void saveSwitchBl_withNullDescription_descriptionIsNull() {
        HouseBlJpaEntity houseBl = newHouseBl();
        em.persist(houseBl);
        em.flush();

        SwitchBlJpaEntity switchBl = newSwitchBl(houseBl);
        em.persist(switchBl);
        em.flush();
        em.clear();

        // SwitchBlJpaEntity.description는 mappedBy 측 — persist 없이 null 상태 유지
        SwitchBlJpaEntity loaded = em.find(SwitchBlJpaEntity.class, switchBl.getSwitchBlId());

        assertThat(loaded.getDescription()).isNull();
    }

    @Test
    @DisplayName("SwitchBl blType/incoterms 저장 후 flush+clear → 재조회 시 값이 유지된다")
    void saveSwitchBl_withBlTypeAndIncoterms_roundTrip() {
        HouseBlJpaEntity houseBl = newHouseBl();
        em.persist(houseBl);
        em.flush();

        SwitchBlJpaEntity switchBl = newSwitchBl(houseBl);
        switchBl.setBlType("OBL");
        switchBl.setIncoterms("FOB");
        em.persist(switchBl);
        em.flush();
        em.clear();

        SwitchBlJpaEntity loaded = em.find(SwitchBlJpaEntity.class, switchBl.getSwitchBlId());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getBlType()).isEqualTo("OBL");
        assertThat(loaded.getIncoterms()).isEqualTo("FOB");
    }

    @Test
    @DisplayName("SwitchBl blType/incoterms null 저장 후 flush+clear → 재조회 시 null 이다")
    void saveSwitchBl_withNullBlTypeAndIncoterms_roundTrip() {
        HouseBlJpaEntity houseBl = newHouseBl();
        em.persist(houseBl);
        em.flush();

        SwitchBlJpaEntity switchBl = newSwitchBl(houseBl);
        em.persist(switchBl);
        em.flush();
        em.clear();

        SwitchBlJpaEntity loaded = em.find(SwitchBlJpaEntity.class, switchBl.getSwitchBlId());

        assertThat(loaded).isNotNull();
        assertThat(loaded.getBlType()).isNull();
        assertThat(loaded.getIncoterms()).isNull();
    }

    @Test
    @DisplayName("SwitchBl em.remove 후 flush → em.find == null")
    void deleteSwitchBl_removesEntityFromDb() {
        HouseBlJpaEntity houseBl = newHouseBl();
        em.persist(houseBl);
        em.flush();

        SwitchBlJpaEntity switchBl = newSwitchBl(houseBl);
        em.persist(switchBl);
        em.flush();
        em.clear();

        Long switchBlId = switchBl.getSwitchBlId();
        SwitchBlJpaEntity toDelete = em.find(SwitchBlJpaEntity.class, switchBlId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        assertThat(em.find(SwitchBlJpaEntity.class, switchBlId)).isNull();
    }
}
