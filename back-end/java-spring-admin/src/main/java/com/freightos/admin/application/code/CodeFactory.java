package com.freightos.admin.application.code;

import com.freightos.admin.application.code.command.CreateCodeCommand;
import com.freightos.admin.domain.code.entity.Code;
import org.springframework.stereotype.Component;

@Component
public class CodeFactory {
    public Code from(CreateCodeCommand command) {
        return Code.create(
                command.codeGroup(),
                command.codeValue(),
                command.codeLabel(),
                command.sortOrder(),
                command.active(),
                command.remark()
        );
    }
}
