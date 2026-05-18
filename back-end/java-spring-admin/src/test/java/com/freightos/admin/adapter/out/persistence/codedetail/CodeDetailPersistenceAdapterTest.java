package com.freightos.admin.adapter.out.persistence.codedetail;

import com.freightos.admin.application.codedetail.command.SearchCodeDetailCommand;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
import com.freightos.admin.common.config.JpaAuditingConfig;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codedetail.entity.CodeDetail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Import({CodeDetailPersistenceAdapter.class, CodeDetailDomainToJpaMapper.class, CodeDetailJpaToDomainMapper.class, CodeDetailRepositoryImpl.class, JpaAuditingConfig.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:codedetailpa;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS admin",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=admin",
        "spring.flyway.enabled=false"
})
class CodeDetailPersistenceAdapterTest {

    @Autowired
    private CodeDetailPersistenceAdapter adapter;

    @Autowired
    private CodeDetailRepository codeDetailRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ── save → id 반환 + findCodeDetailById 재조회 + 필드 일치 ──────────────

    @Test
    void save_thenFindById_fieldsMatch() {
        CodeDetail codeDetail = CodeDetail.create(1L, "ACTIVE", "활성", 1, true, "활성 상태 코드");

        Long id = adapter.save(codeDetail);

        Optional<CodeDetail> found = adapter.findCodeDetailById(id);
        assertThat(found).isPresent();
        CodeDetail result = found.get();
        assertThat(result.getMasterId()).isEqualTo(1L);
        assertThat(result.getCodeValue()).isEqualTo("ACTIVE");
        assertThat(result.getCodeLabel()).isEqualTo("활성");
        assertThat(result.getSortOrder()).isEqualTo(1);
        assertThat(result.getActive()).isTrue();
        assertThat(result.getRemark()).isEqualTo("활성 상태 코드");
        // id는 동적 참조 (T1: 시퀀스 절대값 비교 금지)
        assertThat(result.getId()).isEqualTo(id);
    }

    // ── searchSummaries: masterId 조건·정렬 검증 ─────────────────────────────

    @Test
    void searchSummaries_byMasterId_returnsSortedPage() {
        adapter.save(CodeDetail.create(1L, "INACTIVE", "비활성", 2, true, null));
        adapter.save(CodeDetail.create(1L, "ACTIVE", "활성", 1, true, null));
        adapter.save(CodeDetail.create(2L, "PENDING", "대기", 1, true, null));

        SearchCodeDetailCommand command = new SearchCodeDetailCommand(1L, null, null, null, 0, 20);

        PagedResult<CodeDetailSummary> result = adapter.searchSummaries(command);

        assertThat(result.getTotalElements()).isEqualTo(2L);
        // sortOrder asc, codeValue asc 정렬 — sortOrder 1인 ACTIVE가 먼저
        assertThat(result.getContent().get(0).codeValue()).isEqualTo("ACTIVE");
        assertThat(result.getContent().get(1).codeValue()).isEqualTo("INACTIVE");
    }

    // ── update: applyUpdate 반영 검증 ─────────────────────────────────────────

    @Test
    void update_appliesUpdateFields() {
        CodeDetail original = CodeDetail.create(1L, "ACTIVE", "원본 레이블", 1, true, "원본 비고");
        Long id = adapter.save(original);

        CodeDetail patch = CodeDetail.create(1L, "ACTIVE", "수정된 레이블", 5, false, "수정된 비고");
        adapter.update(id, patch);

        Optional<CodeDetail> found = adapter.findCodeDetailById(id);
        assertThat(found).isPresent();
        CodeDetail updated = found.get();
        assertThat(updated.getCodeLabel()).isEqualTo("수정된 레이블");
        assertThat(updated.getSortOrder()).isEqualTo(5);
        assertThat(updated.getActive()).isFalse();
        assertThat(updated.getRemark()).isEqualTo("수정된 비고");
        // 식별 필드는 변경되지 않아야 한다
        assertThat(updated.getMasterId()).isEqualTo(1L);
        assertThat(updated.getCodeValue()).isEqualTo("ACTIVE");
    }

    // ── deleteCodeDetailById: 호출 후 findCodeDetailById empty ───────────────

    @Test
    void deleteCodeDetailById_thenFindById_returnsEmpty() {
        Long id = adapter.save(CodeDetail.create(1L, "ACTIVE", "활성", 1, true, null));

        adapter.deleteCodeDetailById(id);

        assertThat(adapter.findCodeDetailById(id)).isEmpty();
    }

    // ── findCodeDetailById: 미존재 → empty ───────────────────────────────────

    @Test
    void findCodeDetailById_notExist_returnsEmpty() {
        Optional<CodeDetail> result = adapter.findCodeDetailById(99999L);

        assertThat(result).isEmpty();
    }

    // ── deleteCodeDetailById: 존재하지 않는 id → ApplicationException(404) ───

    @Test
    void deleteCodeDetailById_notExist_throwsNotFound() {
        assertThatThrownBy(() -> adapter.deleteCodeDetailById(99999L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── countByMasterId: masterId별 건수 검증 ─────────────────────────────────

    @Test
    void countByMasterId_returnsCorrectCount() {
        adapter.save(CodeDetail.create(10L, "ACTIVE", "활성", 1, true, null));
        adapter.save(CodeDetail.create(10L, "INACTIVE", "비활성", 2, true, null));
        adapter.save(CodeDetail.create(20L, "PENDING", "대기", 1, true, null));

        assertThat(adapter.countByMasterId(10L)).isEqualTo(2L);
        assertThat(adapter.countByMasterId(20L)).isEqualTo(1L);
        assertThat(adapter.countByMasterId(99L)).isEqualTo(0L);
    }
}
