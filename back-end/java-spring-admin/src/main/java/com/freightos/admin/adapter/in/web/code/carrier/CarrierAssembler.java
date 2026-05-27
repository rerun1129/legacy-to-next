package com.freightos.admin.adapter.in.web.code.carrier;

import com.freightos.admin.adapter.in.web.code.carrier.dto.CarrierDetailResponse;
import com.freightos.admin.adapter.in.web.code.carrier.dto.CarrierSummaryResponse;
import com.freightos.admin.adapter.in.web.code.carrier.dto.CreateCarrierRequest;
import com.freightos.admin.adapter.in.web.code.carrier.dto.SaveCarrierChangesRequest;
import com.freightos.admin.adapter.in.web.code.carrier.dto.SearchCarrierRequest;
import com.freightos.admin.adapter.in.web.code.carrier.dto.UpdateCarrierRequest;
import com.freightos.admin.application.code.carrier.command.CreateCarrierCommand;
import com.freightos.admin.application.code.carrier.command.SaveCarrierChangesCommand;
import com.freightos.admin.application.code.carrier.command.SearchCarrierCommand;
import com.freightos.admin.application.code.carrier.command.UpdateCarrierCommand;
import com.freightos.admin.application.code.carrier.projection.CarrierSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.carrier.entity.Carrier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CarrierAssembler {

    public SearchCarrierCommand toSearchCommand(SearchCarrierRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchCarrierCommand(req.carrierCode(), req.name(), req.carrierType(), req.scope(), req.page(), size);
    }

    public CreateCarrierCommand toCreateCommand(CreateCarrierRequest req) {
        return new CreateCarrierCommand(req.carrierCode(), req.name(), req.nameEn(), req.carrierType(), req.carrierAddress(), req.ediCode(), req.active());
    }

    public UpdateCarrierCommand toUpdateCommand(UpdateCarrierRequest req) {
        return new UpdateCarrierCommand(req.name(), req.nameEn(), req.carrierType(), req.carrierAddress(), req.ediCode(), req.active());
    }

    public CarrierSummaryResponse toSummaryResponse(CarrierSummary p) {
        return new CarrierSummaryResponse(p.id(), p.carrierCode(), p.name(), p.nameEn(), p.carrierType(), p.carrierAddress(), p.ediCode(), p.active(), p.deletedAt(), p.updatedAt());
    }

    public CarrierDetailResponse toDetail(Carrier domain) {
        return new CarrierDetailResponse(
                domain.getId(), domain.getCarrierCode(), domain.getName(), domain.getNameEn(),
                domain.getCarrierType(), domain.getCarrierAddress(), domain.getEdiCode(),
                domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<CarrierSummaryResponse> toSummaryPage(PagedResult<CarrierSummary> src) {
        return src.map(this::toSummaryResponse);
    }

    public SaveCarrierChangesCommand toSaveChangesCommand(SaveCarrierChangesRequest req) {
        List<CreateCarrierCommand> creates = req.creates() == null ? List.of()
                : req.creates().stream().map(this::toCreateCommand).toList();
        List<SaveCarrierChangesCommand.UpdateEntry> updates = req.updates() == null ? List.of()
                : req.updates().stream()
                        .map(u -> new SaveCarrierChangesCommand.UpdateEntry(u.id(), new UpdateCarrierCommand(u.name(), u.nameEn(), u.carrierType(), u.carrierAddress(), u.ediCode(), u.active())))
                        .toList();
        List<Long> deleteIds = req.deleteIds() == null ? List.of() : req.deleteIds();
        return new SaveCarrierChangesCommand(creates, updates, deleteIds);
    }
}
