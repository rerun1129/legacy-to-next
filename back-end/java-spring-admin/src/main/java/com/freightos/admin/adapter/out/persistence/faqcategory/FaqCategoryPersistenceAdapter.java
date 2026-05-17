package com.freightos.admin.adapter.out.persistence.faqcategory;

import com.freightos.admin.application.faqcategory.command.SearchFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.port.out.FaqCategoryPort;
import com.freightos.admin.application.faqcategory.projection.FaqCategorySummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faqcategory.entity.FaqCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FaqCategoryPersistenceAdapter implements FaqCategoryPort {

    private final FaqCategoryRepository faqCategoryRepository;
    private final FaqCategoryDomainToJpaMapper domainToJpaMapper;
    private final FaqCategoryJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<FaqCategorySummary> searchSummaries(SearchFaqCategoryCommand command) {
        return faqCategoryRepository.searchSummaries(command);
    }

    @Override
    public Optional<FaqCategory> findById(Long id) {
        return faqCategoryRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(FaqCategory faqCategory) {
        FaqCategoryJpaEntity entity = domainToJpaMapper.toNewJpa(faqCategory);
        faqCategoryRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, FaqCategory patchData) {
        FaqCategoryJpaEntity entity = faqCategoryRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("FAQ_CATEGORY_NOT_FOUND", MessageCode.FAQ_CATEGORY_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        FaqCategoryJpaEntity entity = faqCategoryRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("FAQ_CATEGORY_NOT_FOUND", MessageCode.FAQ_CATEGORY_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
    }

    @Override
    public boolean existsByName(String name) {
        return faqCategoryRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameExcludingId(String name, Long id) {
        return faqCategoryRepository.existsByNameExcludingId(name, id);
    }
}
