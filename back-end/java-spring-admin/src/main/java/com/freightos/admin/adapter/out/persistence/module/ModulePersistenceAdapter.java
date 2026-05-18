package com.freightos.admin.adapter.out.persistence.module;

import com.freightos.admin.application.module.command.SearchModuleCommand;
import com.freightos.admin.application.module.port.out.ModulePort;
import com.freightos.admin.application.module.projection.ModuleSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.module.entity.Module;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ModulePersistenceAdapter implements ModulePort {

    private final ModuleRepository moduleRepository;
    private final ModuleDomainToJpaMapper moduleDomainToJpaMapper;
    private final ModuleJpaToDomainMapper moduleJpaToDomainMapper;

    @Override
    public PagedResult<ModuleSummary> searchSummaries(SearchModuleCommand command) {
        return moduleRepository.searchSummaries(command);
    }

    @Override
    public Optional<Module> findModuleByCode(String moduleCode) {
        return moduleRepository.findByModuleCode(moduleCode).map(moduleJpaToDomainMapper::toDomain);
    }

    @Override
    public String save(Module module) {
        ModuleJpaEntity entity = moduleDomainToJpaMapper.toNewJpa(module);
        moduleRepository.save(entity);
        return entity.getModuleCode();
    }

    @Override
    public void update(String moduleCode, Module patchData) {
        ModuleJpaEntity entity = moduleRepository.findByModuleCode(moduleCode)
                .orElseThrow(() -> ApplicationException.notFound("MODULE_NOT_FOUND", MessageCode.MODULE_NOT_FOUND.getMessage()));
        moduleDomainToJpaMapper.applyUpdateFields(entity, patchData);
    }

    @Override
    public void deleteModuleByCode(String moduleCode) {
        ModuleJpaEntity entity = moduleRepository.findByModuleCode(moduleCode)
                .orElseThrow(() -> ApplicationException.notFound("MODULE_NOT_FOUND", MessageCode.MODULE_NOT_FOUND.getMessage()));
        moduleRepository.delete(entity);
    }

    @Override
    public boolean existsByCode(String moduleCode) {
        return moduleRepository.existsByModuleCode(moduleCode);
    }
}
