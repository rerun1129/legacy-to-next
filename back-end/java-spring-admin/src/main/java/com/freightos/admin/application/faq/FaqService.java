package com.freightos.admin.application.faq;

import com.freightos.admin.application.faq.command.CreateFaqCommand;
import com.freightos.admin.application.faq.command.SearchFaqCommand;
import com.freightos.admin.application.faq.command.UpdateFaqCommand;
import com.freightos.admin.application.faq.port.in.FaqUseCase;
import com.freightos.admin.application.faq.port.out.FaqPort;
import com.freightos.admin.application.faq.projection.FaqSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faq.entity.Faq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService implements FaqUseCase {

    private final FaqPort faqPort;
    private final FaqFactory faqFactory;

    @Override
    public PagedResult<FaqSummary> searchFaqs(SearchFaqCommand command) {
        return faqPort.searchSummaries(command);
    }

    @Override
    public Faq getFaqById(Long id) {
        return faqPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("FAQ_NOT_FOUND", MessageCode.FAQ_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createFaq(CreateFaqCommand command) {
        return faqPort.save(faqFactory.from(command));
    }

    @Override
    @Transactional
    public void updateFaq(Long id, UpdateFaqCommand command) {
        Faq existing = getFaqById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("FAQ_ALREADY_DELETED", MessageCode.FAQ_ALREADY_DELETED.getMessage());
        }
        existing.applyUpdate(command.faqCategoryId(), command.question(), command.answer(), command.sortOrder(), command.active());
        faqPort.update(id, existing);
    }

    @Override
    @Transactional
    public void deleteFaq(Long id) {
        Faq existing = getFaqById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("FAQ_ALREADY_DELETED", MessageCode.FAQ_ALREADY_DELETED.getMessage());
        }
        faqPort.softDelete(id);
    }
}
