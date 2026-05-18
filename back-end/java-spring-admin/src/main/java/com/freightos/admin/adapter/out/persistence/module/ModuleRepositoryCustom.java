package com.freightos.admin.adapter.out.persistence.module;

import com.freightos.admin.application.module.command.SearchModuleCommand;
import com.freightos.admin.application.module.projection.ModuleSummary;
import com.freightos.admin.common.response.PagedResult;

public interface ModuleRepositoryCustom {
    PagedResult<ModuleSummary> searchSummaries(SearchModuleCommand command);
}
