package com.freightos.admin.application.codemaster;

import com.freightos.admin.application.codemaster.command.CreateCodeMasterCommand;
import com.freightos.admin.domain.codemaster.entity.CodeMaster;
import org.springframework.stereotype.Component;

@Component
public class CodeMasterFactory {

    public CodeMaster from(CreateCodeMasterCommand command) {
        return CodeMaster.create(
                command.masterCode(),
                command.masterName(),
                command.description(),
                command.sortOrder(),
                command.active()
        );
    }
}
