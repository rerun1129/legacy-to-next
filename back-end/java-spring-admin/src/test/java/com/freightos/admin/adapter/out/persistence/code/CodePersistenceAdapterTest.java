package com.freightos.admin.adapter.out.persistence.code;

import com.freightos.admin.application.code.command.SearchCodeCommand;
import com.freightos.admin.application.code.projection.CodeSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.entity.Code;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.freightos.admin.common.config.JpaAuditingConfig;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Import({CodePersistenceAdapter.class, CodeDomainToJpaMapper.class, CodeJpaToDomainMapper.class, CodeRepositoryImpl.class, JpaAuditingConfig.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:codepa;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS admin",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=admin",
        "spring.flyway.enabled=false"
})
class CodePersistenceAdapterTest {

    @Autowired
    private CodePersistenceAdapter adapter;

    @Autowired
    private CodeRepository codeRepository;

    // ── save → id 반환 + findById 재조회 + 필드 일치 ─────────────────────────

    @Test
    void save_thenFindById_fieldsMatch() {
        Code code = Code.create("CARRIER", "KR001", "Korean Carrier", 1, true, "국내 운송사");

        Long id = adapter.save(code);

        Optional<Code> found = adapter.findById(id);
        assertThat(found).isPresent();
        Code result = found.get();
        assertThat(result.getCodeGroup()).isEqualTo("CARRIER");
        assertThat(result.getCodeValue()).isEqualTo("KR001");
        assertThat(result.getCodeLabel()).isEqualTo("Korean Carrier");
        assertThat(result.getSortOrder()).isEqualTo(1);
        assertThat(result.getActive()).isTrue();
        assertThat(result.getRemark()).isEqualTo("국내 운송사");
        // id는 동적 참조 (T1: 시퀀스 절대값 비교 금지)
        assertThat(result.getId()).isEqualTo(id);
    }

    // ── searchSummaries: 정렬·페이지 결과 검증 ────────────────────────────────

    @Test
    void searchSummaries_partialMatch_returnsSortedPage() {
        adapter.save(Code.create("CARRIER", "KR001", "Korean Carrier A", 2, true, null));
        adapter.save(Code.create("CARRIER", "KR002", "Korean Carrier B", 1, true, null));
        adapter.save(Code.create("PORT", "KRPUS", "Busan Port", 1, false, null));

        SearchCodeCommand command = new SearchCodeCommand("CARRIER", null, null, null, 0, 20);

        PagedResult<CodeSummary> result = adapter.searchSummaries(command);

        assertThat(result.getTotalElements()).isEqualTo(2L);
        // codeGroup asc, codeValue asc 정렬 — KR001이 먼저
        assertThat(result.getContent().get(0).codeValue()).isEqualTo("KR001");
        assertThat(result.getContent().get(1).codeValue()).isEqualTo("KR002");
    }

    // ── update: applyUpdate 반영 검증 ─────────────────────────────────────────

    @Test
    void update_appliesUpdateFields() {
        Code original = Code.create("CARRIER", "KR001", "Original Label", 1, true, "original remark");
        Long id = adapter.save(original);

        Code patch = Code.create("CARRIER", "KR001", "Updated Label", 5, false, "updated remark");
        adapter.update(id, patch);

        Optional<Code> found = adapter.findById(id);
        assertThat(found).isPresent();
        Code updated = found.get();
        assertThat(updated.getCodeLabel()).isEqualTo("Updated Label");
        assertThat(updated.getSortOrder()).isEqualTo(5);
        assertThat(updated.getActive()).isFalse();
        assertThat(updated.getRemark()).isEqualTo("updated remark");
        // 식별 필드는 변경되지 않아야 한다
        assertThat(updated.getCodeGroup()).isEqualTo("CARRIER");
        assertThat(updated.getCodeValue()).isEqualTo("KR001");
    }

    // ── deleteById: 호출 후 findById empty ────────────────────────────────────

    @Test
    void deleteById_thenFindById_returnsEmpty() {
        Long id = adapter.save(Code.create("CARRIER", "KR001", "Korean Carrier", 1, true, null));

        adapter.deleteById(id);

        assertThat(adapter.findById(id)).isEmpty();
    }

    // ── findById: id 미존재 → empty ───────────────────────────────────────────

    @Test
    void findById_notExist_returnsEmpty() {
        Optional<Code> result = adapter.findById(99999L);

        assertThat(result).isEmpty();
    }

    // ── deleteById: 존재하지 않는 id → ApplicationException(404) ─────────────

    @Test
    void deleteById_notExist_throwsNotFound() {
        assertThatThrownBy(() -> adapter.deleteById(99999L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
