package com.freightos.admin.application.module;

import com.freightos.admin.application.menu.port.out.MenuPort;
import com.freightos.admin.application.module.command.CreateModuleCommand;
import com.freightos.admin.application.module.command.SearchModuleCommand;
import com.freightos.admin.application.module.command.UpdateModuleCommand;
import com.freightos.admin.application.module.port.in.ModuleUseCase;
import com.freightos.admin.application.module.port.out.ModulePort;
import com.freightos.admin.application.module.projection.ModuleSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.module.entity.Module;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModuleService implements ModuleUseCase {

    private final ModulePort modulePort;
    private final ModuleFactory moduleFactory;
    private final MenuPort menuPort;

    @Override
    public PagedResult<ModuleSummary> searchModules(SearchModuleCommand command) {
        return modulePort.searchSummaries(command);
    }

    @Override
    public Module findModuleByCode(String moduleCode) {
        return modulePort.findModuleByCode(moduleCode)
                .orElseThrow(() -> ApplicationException.notFound("MODULE_NOT_FOUND", MessageCode.MODULE_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public String createModule(CreateModuleCommand command) {
        if (modulePort.existsByCode(command.moduleCode())) {
            throw ApplicationException.conflict("MODULE_DUPLICATE_CODE", MessageCode.MODULE_DUPLICATE_CODE.getMessage());
        }
        Module module = moduleFactory.from(command);
        return modulePort.save(module);
    }

    @Override
    @Transactional
    public void updateModule(String moduleCode, UpdateModuleCommand command) {
        Module existing = modulePort.findModuleByCode(moduleCode)
                .orElseThrow(() -> ApplicationException.notFound("MODULE_NOT_FOUND", MessageCode.MODULE_NOT_FOUND.getMessage()));
        existing.applyUpdate(command.name(), command.description(), command.sortOrder(), command.active());
        modulePort.update(moduleCode, existing);
    }

    @Override
    @Transactional
    public void deleteModulesByCodes(List<String> codes) {
        for (String code : codes) {
            deleteModuleByCode(code);
        }
    }

    @Override
    @Transactional
    public void deleteModuleByCode(String moduleCode) {
        if (!modulePort.existsByCode(moduleCode)) {
            throw ApplicationException.notFound("MODULE_NOT_FOUND", MessageCode.MODULE_NOT_FOUND.getMessage());
        }
        // 해당 module_code를 참조하는 메뉴 존재 시 삭제 불가
        if (menuPort.existsByModuleCode(moduleCode)) {
            throw ApplicationException.conflict("MODULE_HAS_MENU_CANNOT_DELETE", MessageCode.MODULE_HAS_MENU_CANNOT_DELETE.getMessage());
        }
        modulePort.deleteModuleByCode(moduleCode);
    }
}
