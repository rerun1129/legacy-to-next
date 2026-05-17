package com.freightos.admin.application.terms.port.in;

import com.freightos.admin.application.terms.command.CreateTermsCommand;
import com.freightos.admin.application.terms.command.SearchTermsCommand;
import com.freightos.admin.application.terms.command.UpdateTermsCommand;
import com.freightos.admin.application.terms.projection.TermsSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.terms.entity.Terms;
import com.freightos.admin.domain.terms.entity.TermsType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TermsUseCase {
    PagedResult<TermsSummary> searchTerms(SearchTermsCommand command);
    Terms getTermsById(Long id);
    Long createTerms(CreateTermsCommand command);
    void updateTerms(Long id, UpdateTermsCommand command);
    void deleteTerms(Long id);
    Optional<Terms> findActiveByType(TermsType type, LocalDateTime asOf);
}
