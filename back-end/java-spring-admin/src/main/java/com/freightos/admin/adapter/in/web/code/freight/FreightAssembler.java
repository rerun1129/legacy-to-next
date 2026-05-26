package com.freightos.admin.adapter.in.web.code.freight;

import com.freightos.admin.adapter.in.web.code.freight.dto.CreateFreightRequest;
import com.freightos.admin.adapter.in.web.code.freight.dto.FreightDetailResponse;
import com.freightos.admin.adapter.in.web.code.freight.dto.FreightSummaryResponse;
import com.freightos.admin.adapter.in.web.code.freight.dto.SearchFreightRequest;
import com.freightos.admin.adapter.in.web.code.freight.dto.UpdateFreightRequest;
import com.freightos.admin.application.code.freight.command.CreateFreightCommand;
import com.freightos.admin.application.code.freight.command.SearchFreightCommand;
import com.freightos.admin.application.code.freight.command.UpdateFreightCommand;
import com.freightos.admin.application.code.freight.projection.FreightSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.freight.entity.Freight;
import org.springframework.stereotype.Component;

@Component
public class FreightAssembler {

    public SearchFreightCommand toSearchCommand(SearchFreightRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchFreightCommand(req.freightCode(), req.name(), req.scope(), req.page(), size);
    }

    public CreateFreightCommand toCreateCommand(CreateFreightRequest req) {
        return new CreateFreightCommand(req.freightCode(), req.name(), req.nameEn(), req.description(), req.active());
    }

    public UpdateFreightCommand toUpdateCommand(UpdateFreightRequest req) {
        return new UpdateFreightCommand(req.name(), req.nameEn(), req.description(), req.active());
    }

    public FreightSummaryResponse toSummaryResponse(FreightSummary p) {
        return new FreightSummaryResponse(p.id(), p.freightCode(), p.name(), p.active(), p.deletedAt(), p.updatedAt());
    }

    public FreightDetailResponse toDetail(Freight domain) {
        return new FreightDetailResponse(
                domain.getId(), domain.getFreightCode(), domain.getName(), domain.getNameEn(),
                domain.getDescription(), domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<FreightSummaryResponse> toSummaryPage(PagedResult<FreightSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
