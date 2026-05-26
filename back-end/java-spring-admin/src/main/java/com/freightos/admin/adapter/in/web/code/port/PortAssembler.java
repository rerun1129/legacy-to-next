package com.freightos.admin.adapter.in.web.code.port;

import com.freightos.admin.adapter.in.web.code.port.dto.CreatePortRequest;
import com.freightos.admin.adapter.in.web.code.port.dto.PortDetailResponse;
import com.freightos.admin.adapter.in.web.code.port.dto.PortSummaryResponse;
import com.freightos.admin.adapter.in.web.code.port.dto.SearchPortRequest;
import com.freightos.admin.adapter.in.web.code.port.dto.UpdatePortRequest;
import com.freightos.admin.application.code.port.command.CreatePortCommand;
import com.freightos.admin.application.code.port.command.SearchPortCommand;
import com.freightos.admin.application.code.port.command.UpdatePortCommand;
import com.freightos.admin.application.code.port.projection.PortSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.port.entity.Port;
import org.springframework.stereotype.Component;

@Component
public class PortAssembler {

    public SearchPortCommand toSearchCommand(SearchPortRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchPortCommand(req.portCode(), req.name(), req.countryCode(), req.portType(), req.scope(), req.page(), size);
    }

    public CreatePortCommand toCreateCommand(CreatePortRequest req) {
        return new CreatePortCommand(req.portCode(), req.name(), req.nameEn(), req.countryCode(), req.portType(), req.active());
    }

    public UpdatePortCommand toUpdateCommand(UpdatePortRequest req) {
        return new UpdatePortCommand(req.name(), req.nameEn(), req.countryCode(), req.portType(), req.active());
    }

    public PortSummaryResponse toSummaryResponse(PortSummary p) {
        return new PortSummaryResponse(p.id(), p.portCode(), p.name(), p.countryCode(), p.portType(), p.active(), p.deletedAt(), p.updatedAt());
    }

    public PortDetailResponse toDetail(Port domain) {
        return new PortDetailResponse(
                domain.getId(), domain.getPortCode(), domain.getName(), domain.getNameEn(),
                domain.getCountryCode(), domain.getPortType(), domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<PortSummaryResponse> toSummaryPage(PagedResult<PortSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
