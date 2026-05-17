package com.freightos.admin.application.code.port.in;

import com.freightos.admin.application.code.command.CreateCodeCommand;
import com.freightos.admin.application.code.command.SearchCodeCommand;
import com.freightos.admin.application.code.command.UpdateCodeCommand;
import com.freightos.admin.application.code.projection.CodeSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.entity.Code;

public interface CodeUseCase {
    PagedResult<CodeSummary> searchCodes(SearchCodeCommand command);
    Code findCodeById(Long id);
    Long createCode(CreateCodeCommand command);
    void updateCode(Long id, UpdateCodeCommand command);
    void deleteCodeById(Long id);
}
