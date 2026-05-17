package com.freightos.admin.application.partner.port.in;

import com.freightos.admin.application.partner.command.CreatePartnerCommand;
import com.freightos.admin.application.partner.command.SearchPartnerCommand;
import com.freightos.admin.application.partner.command.UpdatePartnerCommand;
import com.freightos.admin.application.partner.projection.PartnerSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.partner.entity.Partner;

public interface PartnerUseCase {
    PagedResult<PartnerSummary> searchPartners(SearchPartnerCommand command);
    Partner getPartnerById(Long id);
    Long createPartner(CreatePartnerCommand command);
    void updatePartner(Long id, UpdatePartnerCommand command);
    void deletePartner(Long id);
}
