package com.freightos.admin.adapter.out.persistence.codemaster;

import com.freightos.admin.application.codemaster.command.SearchCodeMasterCommand;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.common.config.JpaAuditingConfig;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codemaster.entity.CodeMaster;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
@Import({CodeMasterPersistenceAdapter.class, CodeMasterDomainToJpaMapper.class, CodeMasterJpaToDomainMapper.class, CodeMasterRepositoryImpl.class, JpaAuditingConfig.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:codemasterpa;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS admin",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=admin",
        "spring.flyway.enabled=false"
})
class CodeMasterPersistenceAdapterTest {

    @Autowired
    private CodeMasterPersistenceAdapter adapter;

    @Autowired
    private CodeMasterRepository codeMasterRepository;

    // ── save → id 반환 + findCodeMasterById 재조회 + 필드 일치 ────────────────

    @Test
    void save_thenFindById_fieldsMatch() {
        CodeMaster codeMaster = CodeMaster.create("USER_STATUS", "사용자 상태", "사용자의 활성 여부", 1, true);

        Long id = adapter.save(codeMaster);

        Optional<CodeMaster> found = adapter.findCodeMasterById(id);
        assertThat(found).isPresent();
        CodeMaster result = found.get();
        assertThat(result.getMasterCode()).isEqualTo("USER_STATUS");
        assertThat(result.getMasterName()).isEqualTo("사용자 상태");
        assertThat(result.getDescription()).isEqualTo("사용자의 활성 여부");
        assertThat(result.getSortOrder()).isEqualTo(1);
        assertThat(result.getActive()).isTrue();
        // id는 동적 참조 (T1: 시퀀스 절대값 비교 금지)
        assertThat(result.getId()).isEqualTo(id);
    }

    // ── searchSummaries: 정렬·페이지 결과 검증 ────────────────────────────────

    @Test
    void searchSummaries_partialMatch_returnsSortedPage() {
        adapter.save(CodeMaster.create("ORDER_STATUS", "주문 상태", null, 2, true));
        adapter.save(CodeMaster.create("USER_STATUS", "사용자 상태", null, 1, true));
        adapter.save(CodeMaster.create("INACTIVE_GROUP", "비활성 그룹", null, 1, false));

        SearchCodeMasterCommand command = new SearchCodeMasterCommand(null, "상태", null, 0, 20);

        PagedResult<CodeMasterSummary> result = adapter.searchSummaries(command);

        assertThat(result.getTotalElements()).isEqualTo(2L);
        // masterCode asc 정렬 — ORDER_STATUS가 USER_STATUS보다 먼저
        assertThat(result.getContent().get(0).masterCode()).isEqualTo("ORDER_STATUS");
        assertThat(result.getContent().get(1).masterCode()).isEqualTo("USER_STATUS");
    }

    // ── update: applyUpdate 반영 검증 ─────────────────────────────────────────

    @Test
    void update_appliesUpdateFields() {
        CodeMaster original = CodeMaster.create("USER_STATUS", "원본 그룹명", "원본 설명", 1, true);
        Long id = adapter.save(original);

        CodeMaster patch = CodeMaster.create("USER_STATUS", "수정된 그룹명", "수정된 설명", 5, false);
        adapter.update(id, patch);

        Optional<CodeMaster> found = adapter.findCodeMasterById(id);
        assertThat(found).isPresent();
        CodeMaster updated = found.get();
        assertThat(updated.getMasterName()).isEqualTo("수정된 그룹명");
        assertThat(updated.getDescription()).isEqualTo("수정된 설명");
        assertThat(updated.getSortOrder()).isEqualTo(5);
        assertThat(updated.getActive()).isFalse();
        // 식별 필드는 변경되지 않아야 한다
        assertThat(updated.getMasterCode()).isEqualTo("USER_STATUS");
    }

    // ── deleteCodeMasterById: 호출 후 findCodeMasterById empty ───────────────

    @Test
    void deleteCodeMasterById_thenFindById_returnsEmpty() {
        Long id = adapter.save(CodeMaster.create("USER_STATUS", "사용자 상태", null, 1, true));

        adapter.deleteCodeMasterById(id);

        assertThat(adapter.findCodeMasterById(id)).isEmpty();
    }

    // ── findCodeMasterById: 미존재 → empty ───────────────────────────────────

    @Test
    void findCodeMasterById_notExist_returnsEmpty() {
        Optional<CodeMaster> result = adapter.findCodeMasterById(99999L);

        assertThat(result).isEmpty();
    }

    // ── deleteCodeMasterById: 존재하지 않는 id → ApplicationException(404) ────

    @Test
    void deleteCodeMasterById_notExist_throwsNotFound() {
        assertThatThrownBy(() -> adapter.deleteCodeMasterById(99999L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── existsById: 존재/미존재 검증 ─────────────────────────────────────────

    @Test
    void existsById_existsAndNotExists() {
        Long id = adapter.save(CodeMaster.create("USER_STATUS", "사용자 상태", null, 1, true));

        assertThat(adapter.existsById(id)).isTrue();
        assertThat(adapter.existsById(id + 99999L)).isFalse();
    }
}
