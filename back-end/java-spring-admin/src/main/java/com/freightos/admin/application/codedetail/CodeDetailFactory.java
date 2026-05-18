package com.freightos.admin.application.codedetail;

import com.freightos.admin.application.codedetail.command.CreateCodeDetailCommand;
import com.freightos.admin.domain.codedetail.entity.CodeDetail;
import org.springframework.stereotype.Component;

@Component
public class CodeDetailFactory {

    public CodeDetail from(CreateCodeDetailCommand command) {
        return CodeDetail.create(
                command.masterId(),
                command.codeValue(),
                command.codeLabel(),
                command.sortOrder(),
                command.active(),
                command.remark()
        );
    }
}
