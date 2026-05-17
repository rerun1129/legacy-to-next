package com.freightos.admin.adapter.out.persistence.partner;

import com.freightos.admin.application.partner.command.SearchPartnerCommand;
import com.freightos.admin.application.partner.projection.PartnerSummary;
import com.freightos.admin.common.config.JpaAuditingConfig;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.partner.entity.Partner;
import com.freightos.admin.domain.partner.entity.PartnerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({PartnerPersistenceAdapter.class, PartnerDomainToJpaMapper.class, PartnerJpaToDomainMapper.class, PartnerRepositoryImpl.class, JpaAuditingConfig.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:partnerpa;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS admin",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=admin",
        "spring.flyway.enabled=false"
})
class PartnerPersistenceAdapterTest {

    @Autowired
    private PartnerPersistenceAdapter adapter;

    @Autowired
    private PartnerRepository partnerRepository;

    // ── save → id 반환 + findById 재조회 + 필드 일치 ─────────────────────────

    @Test
    void save_thenFindById_fieldsMatch() {
        Partner partner = Partner.create("FWD-001", PartnerType.FORWARDER, "글로벌 포워더",
                "Global Forwarder", "123-45-67890", "홍길동", "010-1234-5678",
                "test@fwd.com", "서울시 강남구", "메모 내용", true);

        Long id = adapter.save(partner);

        Optional<Partner> found = adapter.findById(id);
        assertThat(found).isPresent();
        Partner result = found.get();
        assertThat(result.getPartnerCode()).isEqualTo("FWD-001");
        assertThat(result.getPartnerType()).isEqualTo(PartnerType.FORWARDER);
        assertThat(result.getName()).isEqualTo("글로벌 포워더");
        assertThat(result.isActive()).isTrue();
        assertThat(result.isDeleted()).isFalse();
        // id는 동적 참조 (T1: 시퀀스 절대값 비교 금지)
        assertThat(result.getId()).isEqualTo(id);
    }

    // ── partner_code UNIQUE 위반 → DataIntegrityViolationException ───────────

    @Test
    void save_duplicatePartnerCode_throwsDataIntegrityViolation() {
        Partner first = Partner.create("DUP-001", PartnerType.SHIPPER, "첫 번째 화주",
                null, null, null, null, null, null, null, true);
        adapter.save(first);
        partnerRepository.flush();

        Partner second = Partner.create("DUP-001", PartnerType.CARRIER, "두 번째 운송사",
                null, null, null, null, null, null, null, true);

        assertThatThrownBy(() -> {
            adapter.save(second);
            partnerRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    // ── softDelete 후 findById 는 deletedAt이 있는 도메인 반환 ─────────────────

    @Test
    void softDelete_thenFindById_returnsDomainWithDeletedAt() {
        Partner partner = Partner.create("SHP-001", PartnerType.SHIPPER, "화주 회사",
                null, null, null, null, null, null, null, true);
        Long id = adapter.save(partner);

        adapter.softDelete(id);

        Optional<Partner> found = adapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().isDeleted()).isTrue();
        assertThat(found.get().isActive()).isFalse();
        assertThat(found.get().getDeletedAt()).isNotNull();
    }

    // ── searchSummaries: includeDeleted=false → soft deleted 제외 ────────────

    @Test
    void searchSummaries_excludeDeleted_softDeletedNotReturned() {
        Partner active = Partner.create("ACT-001", PartnerType.AGENT, "활성 에이전트",
                null, null, null, null, null, null, null, true);
        Long activeId = adapter.save(active);

        Partner toDelete = Partner.create("DEL-001", PartnerType.AGENT, "삭제될 에이전트",
                null, null, null, null, null, null, null, true);
        Long deletedId = adapter.save(toDelete);
        adapter.softDelete(deletedId);

        SearchPartnerCommand command = new SearchPartnerCommand(null, null, null, null, false, 0, 20);
        PagedResult<PartnerSummary> result = adapter.searchSummaries(command);

        List<Long> ids = result.getContent().stream().map(PartnerSummary::id).toList();
        assertThat(ids).contains(activeId);
        assertThat(ids).doesNotContain(deletedId);
    }

    // ── searchSummaries: includeDeleted=true → soft deleted 포함 ─────────────

    @Test
    void searchSummaries_includeDeleted_softDeletedReturned() {
        Partner active = Partner.create("INC-ACT", PartnerType.FORWARDER, "포함 활성",
                null, null, null, null, null, null, null, true);
        Long activeId = adapter.save(active);

        Partner deleted = Partner.create("INC-DEL", PartnerType.FORWARDER, "포함 삭제",
                null, null, null, null, null, null, null, true);
        Long deletedId = adapter.save(deleted);
        adapter.softDelete(deletedId);

        SearchPartnerCommand command = new SearchPartnerCommand("INC-", null, null, null, true, 0, 20);
        PagedResult<PartnerSummary> result = adapter.searchSummaries(command);

        List<Long> ids = result.getContent().stream().map(PartnerSummary::id).toList();
        assertThat(ids).contains(activeId, deletedId);
    }

    // ── searchSummaries: partnerType 필터 ────────────────────────────────────

    @Test
    void searchSummaries_filterByPartnerType_returnsOnlyMatchingType() {
        adapter.save(Partner.create("TYPE-FWD", PartnerType.FORWARDER, "포워더 회사", null, null, null, null, null, null, null, true));
        adapter.save(Partner.create("TYPE-SHP", PartnerType.SHIPPER, "화주 회사", null, null, null, null, null, null, null, true));
        adapter.save(Partner.create("TYPE-CSG", PartnerType.CONSIGNEE, "수하인 회사", null, null, null, null, null, null, null, true));

        SearchPartnerCommand command = new SearchPartnerCommand("TYPE-", null, "FORWARDER", null, false, 0, 20);
        PagedResult<PartnerSummary> result = adapter.searchSummaries(command);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).partnerType()).isEqualTo(PartnerType.FORWARDER);
    }

    // ── searchSummaries: 정렬 partner_code asc + id asc tie-break ────────────

    @Test
    void searchSummaries_sortByPartnerCodeAscThenIdAsc() {
        adapter.save(Partner.create("SORT-C", PartnerType.CARRIER, "운송사 C", null, null, null, null, null, null, null, true));
        adapter.save(Partner.create("SORT-A", PartnerType.AGENT, "에이전트 A", null, null, null, null, null, null, null, true));
        adapter.save(Partner.create("SORT-B", PartnerType.CUSTOMS_BROKER, "관세사 B", null, null, null, null, null, null, null, true));

        SearchPartnerCommand command = new SearchPartnerCommand("SORT-", null, null, null, false, 0, 20);
        PagedResult<PartnerSummary> result = adapter.searchSummaries(command);

        assertThat(result.getContent()).hasSize(3);
        // partner_code asc 정렬 — A, B, C 순
        assertThat(result.getContent().get(0).partnerCode()).isEqualTo("SORT-A");
        assertThat(result.getContent().get(1).partnerCode()).isEqualTo("SORT-B");
        assertThat(result.getContent().get(2).partnerCode()).isEqualTo("SORT-C");
    }

    // ── searchSummaries: name 부분일치 LIKE ──────────────────────────────────

    @Test
    void searchSummaries_filterByNameLike_returnsMatchingNames() {
        adapter.save(Partner.create("NAME-001", PartnerType.FORWARDER, "글로벌 물류", null, null, null, null, null, null, null, true));
        adapter.save(Partner.create("NAME-002", PartnerType.SHIPPER, "로컬 화주", null, null, null, null, null, null, null, true));
        adapter.save(Partner.create("NAME-003", PartnerType.CONSIGNEE, "글로벌 수하인", null, null, null, null, null, null, null, true));

        SearchPartnerCommand command = new SearchPartnerCommand(null, "글로벌", null, null, false, 0, 20);
        PagedResult<PartnerSummary> result = adapter.searchSummaries(command);

        assertThat(result.getTotalElements()).isEqualTo(2L);
        List<String> names = result.getContent().stream().map(PartnerSummary::partnerCode).toList();
        assertThat(names).containsExactlyInAnyOrder("NAME-001", "NAME-003");
    }
}
