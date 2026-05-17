package com.freightos.admin.adapter.in.web.partner;

import com.freightos.admin.adapter.in.web.partner.dto.CreatePartnerRequest;
import com.freightos.admin.adapter.in.web.partner.dto.PartnerDetailResponse;
import com.freightos.admin.adapter.in.web.partner.dto.PartnerSummaryResponse;
import com.freightos.admin.adapter.in.web.partner.dto.SearchPartnerRequest;
import com.freightos.admin.adapter.in.web.partner.dto.UpdatePartnerRequest;
import com.freightos.admin.application.partner.command.CreatePartnerCommand;
import com.freightos.admin.application.partner.command.SearchPartnerCommand;
import com.freightos.admin.application.partner.command.UpdatePartnerCommand;
import com.freightos.admin.application.partner.projection.PartnerSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.partner.entity.Partner;
import org.springframework.stereotype.Component;

@Component
public class PartnerAssembler {

    public SearchPartnerCommand toSearchCommand(SearchPartnerRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchPartnerCommand(req.partnerCode(), req.name(), req.partnerType(), req.active(), req.includeDeleted(), req.page(), size);
    }

    public CreatePartnerCommand toCreateCommand(CreatePartnerRequest req) {
        return new CreatePartnerCommand(req.partnerCode(), req.partnerType(), req.name(), req.nameEn(), req.businessNo(), req.representative(), req.phone(), req.email(), req.address(), req.memo(), req.active());
    }

    public UpdatePartnerCommand toUpdateCommand(UpdatePartnerRequest req) {
        return new UpdatePartnerCommand(req.partnerType(), req.name(), req.nameEn(), req.businessNo(), req.representative(), req.phone(), req.email(), req.address(), req.memo(), req.active());
    }

    public PartnerSummaryResponse toSummaryResponse(PartnerSummary p) {
        return new PartnerSummaryResponse(p.id(), p.partnerCode(), p.partnerType(), p.name(), p.active(), p.deletedAt(), p.updatedAt());
    }

    public PartnerDetailResponse toDetail(Partner domain) {
        return new PartnerDetailResponse(
                domain.getId(), domain.getPartnerCode(), domain.getPartnerType(),
                domain.getName(), domain.getNameEn(), domain.getBusinessNo(),
                domain.getRepresentative(), domain.getPhone(), domain.getEmail(),
                domain.getAddress(), domain.getMemo(), domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<PartnerSummaryResponse> toSummaryPage(PagedResult<PartnerSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
