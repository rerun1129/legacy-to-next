package com.freightos.admin.application.terms;

import com.freightos.admin.application.terms.command.CreateTermsCommand;
import com.freightos.admin.application.terms.command.SearchTermsCommand;
import com.freightos.admin.application.terms.command.UpdateTermsCommand;
import com.freightos.admin.application.terms.port.in.TermsUseCase;
import com.freightos.admin.application.terms.port.out.TermsPort;
import com.freightos.admin.application.terms.projection.TermsSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.terms.entity.Terms;
import com.freightos.admin.domain.terms.entity.TermsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService implements TermsUseCase {

    private final TermsPort termsPort;
    private final TermsFactory termsFactory;

    @Override
    public PagedResult<TermsSummary> searchTerms(SearchTermsCommand command) {
        return termsPort.searchSummaries(command);
    }

    @Override
    public Terms getTermsById(Long id) {
        return termsPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("TERMS_NOT_FOUND", MessageCode.TERMS_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createTerms(CreateTermsCommand command) {
        TermsType type = TermsType.valueOf(command.type());
        if (termsPort.existsByTypeAndVersion(type, command.version())) {
            throw ApplicationException.conflict("TERMS_ALREADY_EXISTS", "동일한 type/version 약관이 이미 존재합니다.");
        }
        return termsPort.save(termsFactory.from(command));
    }

    @Override
    @Transactional
    public void updateTerms(Long id, UpdateTermsCommand command) {
        Terms existing = getTermsById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("TERMS_ALREADY_DELETED", MessageCode.TERMS_ALREADY_DELETED.getMessage());
        }
        existing.applyUpdate(command.content(), command.summary(), command.effectiveAt());
        termsPort.update(id, existing);
    }

    @Override
    @Transactional
    public void deleteTerms(Long id) {
        Terms existing = getTermsById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("TERMS_ALREADY_DELETED", MessageCode.TERMS_ALREADY_DELETED.getMessage());
        }
        termsPort.softDelete(id);
    }

    @Override
    public Optional<Terms> findActiveByType(TermsType type, LocalDateTime asOf) {
        return termsPort.findActiveByType(type, asOf);
    }
}
