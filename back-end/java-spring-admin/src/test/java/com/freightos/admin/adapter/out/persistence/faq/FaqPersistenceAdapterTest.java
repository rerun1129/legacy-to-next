package com.freightos.admin.adapter.out.persistence.faq;

import com.freightos.admin.adapter.out.persistence.faqcategory.FaqCategoryDomainToJpaMapper;
import com.freightos.admin.adapter.out.persistence.faqcategory.FaqCategoryJpaToDomainMapper;
import com.freightos.admin.adapter.out.persistence.faqcategory.FaqCategoryPersistenceAdapter;
import com.freightos.admin.adapter.out.persistence.faqcategory.FaqCategoryRepository;
import com.freightos.admin.adapter.out.persistence.faqcategory.FaqCategoryRepositoryImpl;
import com.freightos.admin.application.faq.command.SearchFaqCommand;
import com.freightos.admin.application.faq.projection.FaqSummary;
import com.freightos.admin.application.faqcategory.command.SearchFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.projection.FaqCategorySummary;
import com.freightos.admin.common.config.JpaAuditingConfig;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faq.entity.Faq;
import com.freightos.admin.domain.faqcategory.entity.FaqCategory;
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
@Import({
        FaqPersistenceAdapter.class, FaqDomainToJpaMapper.class, FaqJpaToDomainMapper.class, FaqRepositoryImpl.class,
        FaqCategoryPersistenceAdapter.class, FaqCategoryDomainToJpaMapper.class, FaqCategoryJpaToDomainMapper.class, FaqCategoryRepositoryImpl.class,
        JpaAuditingConfig.class
})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:faqipa;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS admin",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=admin",
        "spring.flyway.enabled=false"
})
class FaqPersistenceAdapterTest {

    @Autowired
    private FaqPersistenceAdapter faqAdapter;

    @Autowired
    private FaqRepository faqRepository;

    @Autowired
    private FaqCategoryPersistenceAdapter faqCategoryAdapter;

    @Autowired
    private FaqCategoryRepository faqCategoryRepository;

    // ── FaqCategory: save → findById 필드 일치 ───────────────────────────────

    @Test
    void faqCategory_save_thenFindById_fieldsMatch() {
        FaqCategory category = FaqCategory.create("기술 질문", 1, true);

        Long id = faqCategoryAdapter.save(category);

        Optional<FaqCategory> found = faqCategoryAdapter.findById(id);
        assertThat(found).isPresent();
        FaqCategory result = found.get();
        assertThat(result.getName()).isEqualTo("기술 질문");
        assertThat(result.getSortOrder()).isEqualTo(1);
        assertThat(result.isActive()).isTrue();
        assertThat(result.isDeleted()).isFalse();
        // id는 동적 참조 (T1: 시퀀스 절대값 비교 금지)
        assertThat(result.getId()).isEqualTo(id);
    }

    // ── FaqCategory: UNIQUE(name) 위반 → DataIntegrityViolationException ──────

    @Test
    void faqCategory_save_duplicateName_throwsDataIntegrity() {
        FaqCategory first = FaqCategory.create("중복카테고리", 0, true);
        faqCategoryAdapter.save(first);
        faqCategoryRepository.flush();

        FaqCategory duplicate = FaqCategory.create("중복카테고리", 1, true);

        assertThatThrownBy(() -> {
            faqCategoryAdapter.save(duplicate);
            faqCategoryRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    // ── FaqCategory: searchSummaries sort_order ASC 정렬 검증 ─────────────────

    @Test
    void faqCategory_searchSummaries_sortOrderAsc_returnsInOrder() {
        faqCategoryAdapter.save(FaqCategory.create("카테고리A", 2, true));
        faqCategoryAdapter.save(FaqCategory.create("카테고리B", 0, true));
        faqCategoryAdapter.save(FaqCategory.create("카테고리C", 1, true));
        faqCategoryRepository.flush();

        SearchFaqCategoryCommand command = new SearchFaqCategoryCommand(null, "ACTIVE", 0, 20);
        PagedResult<FaqCategorySummary> result = faqCategoryAdapter.searchSummaries(command);

        List<Integer> orders = result.getContent().stream().map(FaqCategorySummary::sortOrder).toList();
        // sort_order 오름차순 검증 (절대값 비교 금지, 순서만 확인)
        for (int i = 0; i < orders.size() - 1; i++) {
            assertThat(orders.get(i)).isLessThanOrEqualTo(orders.get(i + 1));
        }
        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(3);
    }

    // ── Faq: save → findById 필드 일치 ───────────────────────────────────────

    @Test
    void faq_save_thenFindById_fieldsMatch() {
        FaqCategory category = FaqCategory.create("일반 질문", 0, true);
        Long categoryId = faqCategoryAdapter.save(category);

        Faq faq = Faq.create(categoryId, "Q1", "A1", 0, true);
        Long id = faqAdapter.save(faq);

        Optional<Faq> found = faqAdapter.findById(id);
        assertThat(found).isPresent();
        Faq result = found.get();
        assertThat(result.getFaqCategoryId()).isEqualTo(categoryId);
        assertThat(result.getQuestion()).isEqualTo("Q1");
        assertThat(result.getAnswer()).isEqualTo("A1");
        assertThat(result.isDeleted()).isFalse();
        // id는 동적 참조 (T1: 시퀀스 절대값 비교 금지)
        assertThat(result.getId()).isEqualTo(id);
    }

    // ── Faq: searchSummaries sort_order ASC 정렬 검증 (3건: 2/0/1) ─────────

    @Test
    void faq_searchSummaries_sortOrderAsc_returnsInOrder() {
        FaqCategory category = FaqCategory.create("정렬테스트카테고리", 0, true);
        Long categoryId = faqCategoryAdapter.save(category);

        faqAdapter.save(Faq.create(categoryId, "Q_sortorder2", "A", 2, true));
        faqAdapter.save(Faq.create(categoryId, "Q_sortorder0", "A", 0, true));
        faqAdapter.save(Faq.create(categoryId, "Q_sortorder1", "A", 1, true));
        faqRepository.flush();

        SearchFaqCommand command = new SearchFaqCommand(categoryId, null, "ACTIVE", 0, 20);
        PagedResult<FaqSummary> result = faqAdapter.searchSummaries(command);

        List<Integer> orders = result.getContent().stream().map(FaqSummary::sortOrder).toList();
        // sort_order 오름차순 검증
        assertThat(orders).containsExactly(0, 1, 2);
    }
}
