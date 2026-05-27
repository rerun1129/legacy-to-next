package com.freightos.admin.adapter.in.web.code.hscode;

import com.freightos.admin.adapter.in.web.code.hscode.dto.CreateHsCodeRequest;
import com.freightos.admin.adapter.in.web.code.hscode.dto.HsCodeDetailResponse;
import com.freightos.admin.adapter.in.web.code.hscode.dto.HsCodeSummaryResponse;
import com.freightos.admin.adapter.in.web.code.hscode.dto.SaveHsCodeChangesRequest;
import com.freightos.admin.adapter.in.web.code.hscode.dto.SearchHsCodeRequest;
import com.freightos.admin.adapter.in.web.code.hscode.dto.UpdateHsCodeRequest;
import com.freightos.admin.application.code.hscode.command.CreateHsCodeCommand;
import com.freightos.admin.application.code.hscode.command.SaveHsCodeChangesCommand;
import com.freightos.admin.application.code.hscode.command.SearchHsCodeCommand;
import com.freightos.admin.application.code.hscode.command.UpdateHsCodeCommand;
import com.freightos.admin.application.code.hscode.projection.HsCodeSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.hscode.entity.HsCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HsCodeAssembler {

    public SearchHsCodeCommand toSearchCommand(SearchHsCodeRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchHsCodeCommand(req.hsCode(), req.name(), req.scope(), req.page(), size);
    }

    public CreateHsCodeCommand toCreateCommand(CreateHsCodeRequest req) {
        return new CreateHsCodeCommand(req.hsCode(), req.name(), req.nameEn(), req.countryCode(), req.active());
    }

    public UpdateHsCodeCommand toUpdateCommand(UpdateHsCodeRequest req) {
        return new UpdateHsCodeCommand(req.name(), req.nameEn(), req.countryCode(), req.active());
    }

    public HsCodeSummaryResponse toSummaryResponse(HsCodeSummary p) {
        return new HsCodeSummaryResponse(p.id(), p.hsCode(), p.name(), p.nameEn(), p.countryCode(), p.active(), p.deletedAt(), p.updatedAt());
    }

    public HsCodeDetailResponse toDetail(HsCode domain) {
        return new HsCodeDetailResponse(
                domain.getId(), domain.getHsCode(), domain.getName(), domain.getNameEn(),
                domain.getCountryCode(), domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<HsCodeSummaryResponse> toSummaryPage(PagedResult<HsCodeSummary> src) {
        return src.map(this::toSummaryResponse);
    }

    public SaveHsCodeChangesCommand toSaveChangesCommand(SaveHsCodeChangesRequest req) {
        List<CreateHsCodeCommand> creates = req.creates() == null ? List.of()
                : req.creates().stream().map(this::toCreateCommand).toList();
        List<SaveHsCodeChangesCommand.UpdateEntry> updates = req.updates() == null ? List.of()
                : req.updates().stream()
                        .map(u -> new SaveHsCodeChangesCommand.UpdateEntry(u.id(), new UpdateHsCodeCommand(u.name(), u.nameEn(), u.countryCode(), u.active())))
                        .toList();
        List<Long> deleteIds = req.deleteIds() == null ? List.of() : req.deleteIds();
        return new SaveHsCodeChangesCommand(creates, updates, deleteIds);
    }
}
