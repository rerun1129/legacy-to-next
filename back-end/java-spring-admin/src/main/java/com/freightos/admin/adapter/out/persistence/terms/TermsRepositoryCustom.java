package com.freightos.admin.adapter.out.persistence.terms;

import com.freightos.admin.application.terms.command.SearchTermsCommand;
import com.freightos.admin.application.terms.projection.TermsSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.terms.entity.TermsType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TermsRepositoryCustom {
    PagedResult<TermsSummary> searchSummaries(SearchTermsCommand command);
    boolean existsByTypeAndVersion(TermsType type, int version);
    Optional<TermsJpaEntity> findActiveByType(TermsType type, LocalDateTime asOf);
}
