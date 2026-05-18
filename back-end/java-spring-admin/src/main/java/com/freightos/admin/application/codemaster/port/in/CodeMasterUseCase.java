package com.freightos.admin.application.codemaster.port.in;

import com.freightos.admin.application.codemaster.command.CreateCodeMasterCommand;
import com.freightos.admin.application.codemaster.command.SearchCodeMasterCommand;
import com.freightos.admin.application.codemaster.command.UpdateCodeMasterCommand;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codemaster.entity.CodeMaster;

public interface CodeMasterUseCase {
    PagedResult<CodeMasterSummary> searchCodeMasters(SearchCodeMasterCommand command);
    CodeMaster findCodeMasterById(Long id);
    Long createCodeMaster(CreateCodeMasterCommand command);
    void updateCodeMaster(Long id, UpdateCodeMasterCommand command);
    void deleteCodeMasterById(Long id);
}
