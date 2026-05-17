package com.freightos.admin.adapter.out.persistence.notice;

import com.freightos.admin.application.notice.command.SearchNoticeCommand;
import com.freightos.admin.application.notice.projection.NoticeSummary;
import com.freightos.admin.common.config.JpaAuditingConfig;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.notice.entity.Notice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({NoticePersistenceAdapter.class, NoticeDomainToJpaMapper.class, NoticeJpaToDomainMapper.class, NoticeRepositoryImpl.class, JpaAuditingConfig.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:noticipa;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS admin",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=admin",
        "spring.flyway.enabled=false"
})
class NoticePersistenceAdapterTest {

    @Autowired
    private NoticePersistenceAdapter adapter;

    @Autowired
    private NoticeRepository noticeRepository;

    // ── save → findById 필드 일치 ─────────────────────────────────────────────

    @Test
    void save_thenFindById_fieldsMatch() {
        Notice notice = Notice.create("공지 제목", "공지 내용", false, true, null, null);

        Long id = adapter.save(notice);

        Optional<Notice> found = adapter.findById(id);
        assertThat(found).isPresent();
        Notice result = found.get();
        assertThat(result.getTitle()).isEqualTo("공지 제목");
        assertThat(result.getContent()).isEqualTo("공지 내용");
        assertThat(result.isPinned()).isFalse();
        assertThat(result.isActive()).isTrue();
        assertThat(result.isDeleted()).isFalse();
        // id는 동적 참조 (T1: 시퀀스 절대값 비교 금지)
        assertThat(result.getId()).isEqualTo(id);
    }

    // ── save plain text content("줄1\n줄2") → 개행 보존 ──────────────────────

    @Test
    void save_plainTextContent_preservesNewlines() {
        String multilineContent = "줄1\n줄2";
        Notice notice = Notice.create("제목", multilineContent, false, true, null, null);

        Long id = adapter.save(notice);
        noticeRepository.flush();

        Optional<Notice> found = adapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo(multilineContent);
    }

    // ── softDelete 후 findById → isDeleted=true ───────────────────────────────

    @Test
    void softDelete_thenFindById_returnsDomainWithDeletedAt() {
        Notice notice = Notice.create("삭제 테스트", "내용", false, true, null, null);
        Long id = adapter.save(notice);

        adapter.softDelete(id);

        Optional<Notice> found = adapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().isDeleted()).isTrue();
        assertThat(found.get().isActive()).isFalse();
        assertThat(found.get().getDeletedAt()).isNotNull();
    }

    // ── searchSummaries scope=ACTIVE → deleted 제외 ───────────────────────────

    @Test
    void searchSummaries_scopeActive_excludesDeleted() {
        Notice active = Notice.create("활성 공지", "내용", false, true, null, null);
        Long activeId = adapter.save(active);

        Notice toDelete = Notice.create("삭제될 공지", "내용", false, true, null, null);
        Long deletedId = adapter.save(toDelete);
        adapter.softDelete(deletedId);

        SearchNoticeCommand command = new SearchNoticeCommand(null, null, "ACTIVE", null, 0, 20);
        PagedResult<NoticeSummary> result = adapter.searchSummaries(command);

        List<Long> ids = result.getContent().stream().map(NoticeSummary::id).toList();
        assertThat(ids).contains(activeId);
        assertThat(ids).doesNotContain(deletedId);
    }

    // ── searchSummaries: pinned DESC 정렬 ────────────────────────────────────

    @Test
    void searchSummaries_pinnedDescSort_pinnedFirstInResult() {
        Notice unpinned1 = Notice.create("일반 공지 1", "내용", false, true, null, null);
        Long unpinned1Id = adapter.save(unpinned1);

        Notice pinned = Notice.create("상단 고정 공지", "내용", true, true, null, null);
        Long pinnedId = adapter.save(pinned);

        Notice unpinned2 = Notice.create("일반 공지 2", "내용", false, true, null, null);
        Long unpinned2Id = adapter.save(unpinned2);

        SearchNoticeCommand command = new SearchNoticeCommand(null, null, "ACTIVE", null, 0, 20);
        PagedResult<NoticeSummary> result = adapter.searchSummaries(command);

        assertThat(result.getContent()).isNotEmpty();
        // pinned=true인 항목이 pinned=false 항목보다 앞에 위치해야 한다
        NoticeSummary first = result.getContent().get(0);
        assertThat(first.id()).isEqualTo(pinnedId);
        // 나머지는 pinned=false
        List<Long> rest = result.getContent().subList(1, result.getContent().size()).stream().map(NoticeSummary::id).toList();
        assertThat(rest).contains(unpinned1Id, unpinned2Id);
    }
}
