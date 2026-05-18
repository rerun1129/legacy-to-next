package com.freightos.admin.application.module;

import com.freightos.admin.application.module.command.CreateModuleCommand;
import com.freightos.admin.domain.module.entity.Module;
import org.springframework.stereotype.Component;

@Component
public class ModuleFactory {

    public Module from(CreateModuleCommand command) {
        return Module.create(
                command.moduleCode(),
                command.name(),
                command.description(),
                command.sortOrder(),
                command.active()
        );
    }
}
