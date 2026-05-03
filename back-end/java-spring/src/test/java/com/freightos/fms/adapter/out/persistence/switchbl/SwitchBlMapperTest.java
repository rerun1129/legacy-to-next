package com.freightos.fms.adapter.out.persistence.switchbl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlDescriptionJpaEntity;
import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlJpaEntity;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.switchbl.entity.SwitchBl;
import com.freightos.fms.domain.switchbl.entity.SwitchBlDescription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwitchBlMapperTest {

    private final SwitchBlMapper mapper = new SwitchBlMapper();

    // ── applyFields (도메인 → JPA) ────────────────────────────────────

    @Test
    @DisplayName("applyFields: 도메인 필수·선택 필드가 모두 JPA 엔티티에 복사된다")
    void applyFields_setsAllMandatoryFields() {
        SwitchBl domain = SwitchBl.create(10L, CustomerCode.of("SHIP01"));
        domain.updateDetails("SBL-001", "ORIGINAL", "FOB",
                CustomerCode.of("SHIP01"), CustomerCode.of("CONS01"), CustomerCode.of("NOTIF01"));

        SwitchBlJpaEntity jpa = new SwitchBlJpaEntity();
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(10L);
        jpa.setHouseBl(houseBlJpa);

        mapper.applyFields(domain, jpa);

        assertThat(jpa.getSwitchBlNo()).isEqualTo("SBL-001");
        assertThat(jpa.getBlType()).isEqualTo("ORIGINAL");
        assertThat(jpa.getIncoterms()).isEqualTo("FOB");
        assertThat(jpa.getShipperCode()).isEqualTo("SHIP01");
        assertThat(jpa.getConsigneeCode()).isEqualTo("CONS01");
        assertThat(jpa.getNotifyCode()).isEqualTo("NOTIF01");
        assertThat(jpa.getHouseBl()).isEqualTo(houseBlJpa);
    }

    @Test
    @DisplayName("applyFields: consigneeCode/notifyCode/blType/incoterms null 이어도 NPE 없이 동작한다")
    void applyFields_nullOptionalFields_doesNotThrow() {
        SwitchBl domain = SwitchBl.create(10L, CustomerCode.of("SHIP01"));
        domain.updateDetails(null, null, null, CustomerCode.of("SHIP01"), null, null);

        SwitchBlJpaEntity jpa = new SwitchBlJpaEntity();

        mapper.applyFields(domain, jpa);

        assertThat(jpa.getSwitchBlNo()).isNull();
        assertThat(jpa.getBlType()).isNull();
        assertThat(jpa.getIncoterms()).isNull();
        assertThat(jpa.getShipperCode()).isEqualTo("SHIP01");
        assertThat(jpa.getConsigneeCode()).isNull();
        assertThat(jpa.getNotifyCode()).isNull();
    }

    @Test
    @DisplayName("applyFields: blType=ORIGINAL 이 JPA 엔티티에 그대로 세팅된다")
    void applyFields_blTypeOriginal_copiedToJpa() {
        SwitchBl domain = SwitchBl.create(5L, CustomerCode.of("SHIP05"));
        domain.updateDetails("SBL-005", "ORIGINAL", null, CustomerCode.of("SHIP05"), null, null);

        SwitchBlJpaEntity jpa = new SwitchBlJpaEntity();

        mapper.applyFields(domain, jpa);

        assertThat(jpa.getBlType()).isEqualTo("ORIGINAL");
    }

    // ── toDomain (JPA → 도메인) ───────────────────────────────────────

    @Test
    @DisplayName("toDomain: JPA 핵심 필드가 도메인 객체에 모두 복사된다")
    void toDomain_mapsAllCoreFields() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(20L);

        SwitchBlJpaEntity jpa = new SwitchBlJpaEntity();
        jpa.setHouseBl(houseBlJpa);
        jpa.setSwitchBlNo("SBL-001");
        jpa.setBlType("SURRENDER");
        jpa.setIncoterms("FOB");
        jpa.setShipperCode("SHIP01");
        jpa.setConsigneeCode("CONS01");
        jpa.setNotifyCode("NOTIF01");

        SwitchBl domain = mapper.toDomain(jpa);

        assertThat(domain.getSwitchBlNo()).isEqualTo("SBL-001");
        assertThat(domain.getBlType()).isEqualTo("SURRENDER");
        assertThat(domain.getIncoterms()).isEqualTo("FOB");
        assertThat(domain.getShipperCode().value()).isEqualTo("SHIP01");
        assertThat(domain.getConsigneeCode().value()).isEqualTo("CONS01");
        assertThat(domain.getNotifyCode().value()).isEqualTo("NOTIF01");
        assertThat(domain.getHouseBlId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("toDomain: switchBlNo/blType/incoterms/consigneeCode/notifyCode null 이면 도메인도 null 로 유지된다")
    void toDomain_nullOptionalFields_returnsDomainWithNulls() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(30L);

        SwitchBlJpaEntity jpa = new SwitchBlJpaEntity();
        jpa.setHouseBl(houseBlJpa);
        jpa.setShipperCode("SHIP02");

        SwitchBl domain = mapper.toDomain(jpa);

        assertThat(domain.getShipperCode().value()).isEqualTo("SHIP02");
        assertThat(domain.getSwitchBlNo()).isNull();
        assertThat(domain.getBlType()).isNull();
        assertThat(domain.getIncoterms()).isNull();
        assertThat(domain.getConsigneeCode()).isNull();
        assertThat(domain.getNotifyCode()).isNull();
    }

    @Test
    @DisplayName("toDomain: description JPA 가 null 이면 도메인 description 도 null 이다")
    void toDomain_nullDescription_domainDescriptionIsNull() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(40L);

        SwitchBlJpaEntity jpa = new SwitchBlJpaEntity();
        jpa.setHouseBl(houseBlJpa);
        jpa.setShipperCode("SHIP03");

        SwitchBl domain = mapper.toDomain(jpa);

        assertThat(domain.getDescription()).isNull();
    }

    // ── toDescriptionJpa (도메인 desc → JPA) ─────────────────────────

    @Test
    @DisplayName("toDescriptionJpa: SwitchBlDescription 텍스트 필드가 모두 JPA 엔티티에 복사된다")
    void toDescriptionJpa_setsAllTextFields() {
        SwitchBlDescription desc = SwitchBlDescription.create(100L);
        desc.updateContent("MARKS-VALUE", "NATURE-VALUE");

        SwitchBlJpaEntity switchBlJpa = new SwitchBlJpaEntity();

        SwitchBlDescriptionJpaEntity jpa = mapper.toDescriptionJpa(desc, switchBlJpa);

        assertThat(jpa.getMarks()).isEqualTo("MARKS-VALUE");
        assertThat(jpa.getNatureQuantity()).isEqualTo("NATURE-VALUE");
        assertThat(jpa.getSwitchBl()).isEqualTo(switchBlJpa);
    }

    @Test
    @DisplayName("toDescriptionJpa: 텍스트 필드 null 이어도 NPE 없이 동작한다")
    void toDescriptionJpa_nullTextFields_doesNotThrow() {
        SwitchBlDescription desc = SwitchBlDescription.create(101L);
        desc.updateContent(null, null);

        SwitchBlJpaEntity switchBlJpa = new SwitchBlJpaEntity();

        SwitchBlDescriptionJpaEntity jpa = mapper.toDescriptionJpa(desc, switchBlJpa);

        assertThat(jpa.getMarks()).isNull();
        assertThat(jpa.getNatureQuantity()).isNull();
    }

    @Test
    @DisplayName("toDescriptionJpa: 도메인 description 으로 생성한 JPA 엔티티를 다시 도메인으로 역매핑하면 동일 값이 유지된다")
    void toDescriptionJpa_roundTrip_preservesAllTextFields() {
        SwitchBlDescription desc = SwitchBlDescription.create(50L);
        desc.updateContent("MARKS-TEXT", "NATURE-TEXT");

        SwitchBlJpaEntity switchBlJpa = new SwitchBlJpaEntity();
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(50L);
        switchBlJpa.setHouseBl(houseBlJpa);
        switchBlJpa.setShipperCode("SHIP04");

        SwitchBlDescriptionJpaEntity descJpa = mapper.toDescriptionJpa(desc, switchBlJpa);

        assertThat(descJpa.getMarks()).isEqualTo("MARKS-TEXT");
        assertThat(descJpa.getNatureQuantity()).isEqualTo("NATURE-TEXT");
        assertThat(descJpa.getSwitchBl()).isEqualTo(switchBlJpa);
    }

    @Test
    @DisplayName("toDomain: houseBl 가 null 이면 getHouseBlId() 에서 NullPointerException 이 발생한다")
    void toDomain_nullHouseBl_throwsNullPointerException() {
        SwitchBlJpaEntity jpa = new SwitchBlJpaEntity();
        jpa.setShipperCode("SHIP05");

        assertThatThrownBy(() -> mapper.toDomain(jpa))
                .isInstanceOf(NullPointerException.class);
    }
}
