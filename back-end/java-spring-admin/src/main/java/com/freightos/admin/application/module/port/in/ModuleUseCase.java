package com.freightos.admin.application.module.port.in;

import com.freightos.admin.application.module.command.CreateModuleCommand;
import com.freightos.admin.application.module.command.SearchModuleCommand;
import com.freightos.admin.application.module.command.UpdateModuleCommand;
import com.freightos.admin.application.module.projection.ModuleSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.module.entity.Module;

import java.util.List;

public interface ModuleUseCase {
    PagedResult<ModuleSummary> searchModules(SearchModuleCommand command);
    Module findModuleByCode(String moduleCode);
    String createModule(CreateModuleCommand command);
    void updateModule(String moduleCode, UpdateModuleCommand command);
    void deleteModuleByCode(String moduleCode);
    void deleteModulesByCodes(List<String> codes);
}
