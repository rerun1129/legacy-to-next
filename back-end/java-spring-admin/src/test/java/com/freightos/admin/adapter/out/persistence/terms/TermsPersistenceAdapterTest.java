package com.freightos.admin.adapter.out.persistence.terms;

import com.freightos.admin.application.terms.command.SearchTermsCommand;
import com.freightos.admin.application.terms.projection.TermsSummary;
import com.freightos.admin.common.config.JpaAuditingConfig;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.terms.entity.Terms;
import com.freightos.admin.domain.terms.entity.TermsType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({TermsPersistenceAdapter.class, TermsDomainToJpaMapper.class, TermsJpaToDomainMapper.class, TermsRepositoryImpl.class, JpaAuditingConfig.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:termsipa;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS admin",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=admin",
        "spring.flyway.enabled=false"
})
class TermsPersistenceAdapterTest {

    @Autowired
    private TermsPersistenceAdapter adapter;

    @Autowired
    private TermsRepository termsRepository;

    // ── save → findById 필드 일치 ─────────────────────────────────────────────

    @Test
    void save_thenFindById_fieldsMatch() {
        LocalDateTime effectiveAt = LocalDateTime.now().minusDays(1);
        Terms terms = Terms.create(TermsType.TOS, 1, effectiveAt, "TOS 본문", "요약");

        Long id = adapter.save(terms);

        Optional<Terms> found = adapter.findById(id);
        assertThat(found).isPresent();
        Terms result = found.get();
        assertThat(result.getType()).isEqualTo(TermsType.TOS);
        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo("TOS 본문");
        assertThat(result.getSummary()).isEqualTo("요약");
        assertThat(result.isDeleted()).isFalse();
        // id는 동적 참조 (T1: 시퀀스 절대값 비교 금지)
        assertThat(result.getId()).isEqualTo(id);
    }

    // ── save: UNIQUE(type, version) 위반 → DataIntegrityViolationException ────

    @Test
    void save_duplicateTypeAndVersion_throwsDataIntegrity() {
        LocalDateTime effectiveAt = LocalDateTime.now().minusDays(1);
        Terms first = Terms.create(TermsType.PRIVACY, 1, effectiveAt, "본문1", null);
        adapter.save(first);
        termsRepository.flush();

        Terms duplicate = Terms.create(TermsType.PRIVACY, 1, effectiveAt, "본문2", null);

        assertThatThrownBy(() -> {
            adapter.save(duplicate);
            termsRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    // ── findActiveByType: 미래 버전 제외, effectiveAt 최대 버전 반환 ──────────

    @Test
    void findActiveByType_picksMaxEffectiveAtBeforeAsOf() {
        LocalDateTime now = LocalDateTime.now();
        // v1: now - 2일 (과거)
        adapter.save(Terms.create(TermsType.TOS, 1, now.minusDays(2), "TOS v1", null));
        // v2: now - 1일 (과거, effectiveAt 최대)
        adapter.save(Terms.create(TermsType.TOS, 2, now.minusDays(1), "TOS v2", null));
        // v3: now + 1일 (미래 → 제외)
        adapter.save(Terms.create(TermsType.TOS, 3, now.plusDays(1), "TOS v3", null));
        termsRepository.flush();

        Optional<Terms> found = adapter.findActiveByType(TermsType.TOS, now);

        assertThat(found).isPresent();
        assertThat(found.get().getVersion()).isEqualTo(2);
        assertThat(found.get().getContent()).isEqualTo("TOS v2");
    }

    // ── findActiveByType: soft-deleted 항목 제외 ──────────────────────────────

    @Test
    void findActiveByType_excludesSoftDeleted() {
        LocalDateTime now = LocalDateTime.now();
        // v1: 과거, soft delete
        Long v1Id = adapter.save(Terms.create(TermsType.MARKETING, 1, now.minusDays(2), "MARKETING v1", null));
        adapter.softDelete(v1Id);
        // v2: 과거, 활성
        adapter.save(Terms.create(TermsType.MARKETING, 2, now.minusDays(1), "MARKETING v2", null));
        termsRepository.flush();

        Optional<Terms> found = adapter.findActiveByType(TermsType.MARKETING, now);

        assertThat(found).isPresent();
        assertThat(found.get().getVersion()).isEqualTo(2);
    }

    // ── searchSummaries scope=ACTIVE → 삭제 row 제외 ─────────────────────────

    @Test
    void searchSummaries_excludeDeleted_scopeActive() {
        LocalDateTime effectiveAt = LocalDateTime.now().minusDays(1);
        Long activeId = adapter.save(Terms.create(TermsType.TOS, 10, effectiveAt, "활성 약관", null));

        Long deletedId = adapter.save(Terms.create(TermsType.TOS, 11, effectiveAt, "삭제 약관", null));
        adapter.softDelete(deletedId);

        SearchTermsCommand command = new SearchTermsCommand(null, "ACTIVE", null, null, 0, 20);
        PagedResult<TermsSummary> result = adapter.searchSummaries(command);

        List<Long> ids = result.getContent().stream().map(TermsSummary::termsId).toList();
        assertThat(ids).contains(activeId);
        assertThat(ids).doesNotContain(deletedId);
    }
}
