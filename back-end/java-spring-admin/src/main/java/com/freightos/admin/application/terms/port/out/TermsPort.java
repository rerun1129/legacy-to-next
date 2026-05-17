package com.freightos.admin.application.terms.port.out;

import com.freightos.admin.application.terms.command.SearchTermsCommand;
import com.freightos.admin.application.terms.projection.TermsSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.terms.entity.Terms;
import com.freightos.admin.domain.terms.entity.TermsType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TermsPort {

    PagedResult<TermsSummary> searchSummaries(SearchTermsCommand command);
    Optional<Terms> findById(Long id);
    Long save(Terms terms);
    void update(Long id, Terms patchData);
    void softDelete(Long id);
    boolean existsByTypeAndVersion(TermsType type, int version);
    Optional<Terms> findActiveByType(TermsType type, LocalDateTime asOf);
}
