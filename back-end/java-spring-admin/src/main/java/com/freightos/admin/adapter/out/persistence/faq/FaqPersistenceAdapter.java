package com.freightos.admin.adapter.out.persistence.faq;

import com.freightos.admin.application.faq.command.SearchFaqCommand;
import com.freightos.admin.application.faq.port.out.FaqPort;
import com.freightos.admin.application.faq.projection.FaqSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faq.entity.Faq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FaqPersistenceAdapter implements FaqPort {

    private final FaqRepository faqRepository;
    private final FaqDomainToJpaMapper domainToJpaMapper;
    private final FaqJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<FaqSummary> searchSummaries(SearchFaqCommand command) {
        return faqRepository.searchSummaries(command);
    }

    @Override
    public Optional<Faq> findById(Long id) {
        return faqRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Faq faq) {
        FaqJpaEntity entity = domainToJpaMapper.toNewJpa(faq);
        faqRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, Faq patchData) {
        FaqJpaEntity entity = faqRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("FAQ_NOT_FOUND", MessageCode.FAQ_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        FaqJpaEntity entity = faqRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("FAQ_NOT_FOUND", MessageCode.FAQ_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
    }
}
