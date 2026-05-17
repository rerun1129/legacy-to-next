package com.freightos.admin.application.partner.port.out;

import com.freightos.admin.application.partner.command.SearchPartnerCommand;
import com.freightos.admin.application.partner.projection.PartnerSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.partner.entity.Partner;

import java.util.Optional;

public interface PartnerPort {
    PagedResult<PartnerSummary> searchSummaries(SearchPartnerCommand command);
    Optional<Partner> findById(Long id);
    Long save(Partner partner);
    void update(Long id, Partner patchData);
    void softDelete(Long id);
}
