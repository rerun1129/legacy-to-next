package com.freightos.admin.adapter.in.web.module;

import com.freightos.admin.adapter.in.web.module.dto.CreateModuleRequest;
import com.freightos.admin.adapter.in.web.module.dto.ModuleDetailResponse;
import com.freightos.admin.adapter.in.web.module.dto.ModuleSummaryResponse;
import com.freightos.admin.adapter.in.web.module.dto.SearchModuleRequest;
import com.freightos.admin.adapter.in.web.module.dto.UpdateModuleRequest;
import com.freightos.admin.application.module.command.CreateModuleCommand;
import com.freightos.admin.application.module.command.SearchModuleCommand;
import com.freightos.admin.application.module.command.UpdateModuleCommand;
import com.freightos.admin.application.module.projection.ModuleSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.module.entity.Module;
import org.springframework.stereotype.Component;

@Component
public class ModuleAssembler {

    public SearchModuleCommand toSearchCommand(SearchModuleRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchModuleCommand(req.moduleCode(), req.name(), req.active(), req.page(), size);
    }

    public CreateModuleCommand toCreateCommand(CreateModuleRequest req) {
        return new CreateModuleCommand(req.moduleCode(), req.name(), req.description(), req.sortOrder(), req.active());
    }

    public UpdateModuleCommand toUpdateCommand(UpdateModuleRequest req) {
        return new UpdateModuleCommand(req.name(), req.description(), req.sortOrder(), req.active());
    }

    public ModuleSummaryResponse toSummaryResponse(ModuleSummary p) {
        return new ModuleSummaryResponse(p.id(), p.moduleCode(), p.name(), p.description(), p.sortOrder(), p.active(), p.updatedAt());
    }

    public ModuleDetailResponse toDetail(Module domain) {
        return new ModuleDetailResponse(
                domain.getId(), domain.getModuleCode(), domain.getName(), domain.getDescription(),
                domain.getSortOrder(), domain.getActive(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<ModuleSummaryResponse> toSummaryPage(PagedResult<ModuleSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
