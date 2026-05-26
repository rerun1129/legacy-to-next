package com.freightos.admin.application.code.hscode;

import com.freightos.admin.application.code.hscode.command.CreateHsCodeCommand;
import com.freightos.admin.domain.code.hscode.entity.HsCode;
import org.springframework.stereotype.Component;

@Component
public class HsCodeFactory {

    public HsCode from(CreateHsCodeCommand command) {
        return HsCode.create(command.hsCode(), command.name(), command.nameEn(), command.active());
    }
}
