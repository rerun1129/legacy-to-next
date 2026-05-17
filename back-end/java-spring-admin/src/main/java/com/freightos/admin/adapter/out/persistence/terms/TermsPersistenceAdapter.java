package com.freightos.admin.adapter.out.persistence.terms;

import com.freightos.admin.application.terms.command.SearchTermsCommand;
import com.freightos.admin.application.terms.port.out.TermsPort;
import com.freightos.admin.application.terms.projection.TermsSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.terms.entity.Terms;
import com.freightos.admin.domain.terms.entity.TermsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TermsPersistenceAdapter implements TermsPort {

    private final TermsRepository termsRepository;
    private final TermsDomainToJpaMapper domainToJpaMapper;
    private final TermsJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<TermsSummary> searchSummaries(SearchTermsCommand command) {
        return termsRepository.searchSummaries(command);
    }

    @Override
    public Optional<Terms> findById(Long id) {
        return termsRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Terms terms) {
        TermsJpaEntity entity = domainToJpaMapper.toNewJpa(terms);
        termsRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, Terms patchData) {
        TermsJpaEntity entity = termsRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("TERMS_NOT_FOUND", MessageCode.TERMS_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        TermsJpaEntity entity = termsRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("TERMS_NOT_FOUND", MessageCode.TERMS_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
    }

    @Override
    public boolean existsByTypeAndVersion(TermsType type, int version) {
        return termsRepository.existsByTypeAndVersion(type, version);
    }

    @Override
    public Optional<Terms> findActiveByType(TermsType type, LocalDateTime asOf) {
        return termsRepository.findActiveByType(type, asOf).map(jpaToDomainMapper::toDomain);
    }
}
