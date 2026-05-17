package com.freightos.admin.application.faq.port.in;

import com.freightos.admin.application.faq.command.CreateFaqCommand;
import com.freightos.admin.application.faq.command.SearchFaqCommand;
import com.freightos.admin.application.faq.command.UpdateFaqCommand;
import com.freightos.admin.application.faq.projection.FaqSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faq.entity.Faq;

public interface FaqUseCase {
    PagedResult<FaqSummary> searchFaqs(SearchFaqCommand command);
    Faq getFaqById(Long id);
    Long createFaq(CreateFaqCommand command);
    void updateFaq(Long id, UpdateFaqCommand command);
    void deleteFaq(Long id);
}
