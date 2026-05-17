package com.freightos.admin.application.faq.port.out;

import com.freightos.admin.application.faq.command.SearchFaqCommand;
import com.freightos.admin.application.faq.projection.FaqSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faq.entity.Faq;

import java.util.Optional;

public interface FaqPort {
    PagedResult<FaqSummary> searchSummaries(SearchFaqCommand command);
    Optional<Faq> findById(Long id);
    Long save(Faq faq);
    void update(Long id, Faq patchData);
    void softDelete(Long id);
}
