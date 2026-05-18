package com.freightos.admin.application.module.port.out;

import com.freightos.admin.application.module.command.SearchModuleCommand;
import com.freightos.admin.application.module.projection.ModuleSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.module.entity.Module;

import java.util.Optional;

public interface ModulePort {
    PagedResult<ModuleSummary> searchSummaries(SearchModuleCommand command);
    Optional<Module> findModuleByCode(String moduleCode);
    String save(Module module);
    void update(String moduleCode, Module patchData);
    void deleteModuleByCode(String moduleCode);
    boolean existsByCode(String moduleCode);
}
