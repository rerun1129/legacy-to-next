package com.freightos.admin.application.code.hscode.port.in;

import com.freightos.admin.application.code.hscode.command.CreateHsCodeCommand;
import com.freightos.admin.application.code.hscode.command.SearchHsCodeCommand;
import com.freightos.admin.application.code.hscode.command.UpdateHsCodeCommand;
import com.freightos.admin.application.code.hscode.projection.HsCodeSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.hscode.entity.HsCode;

import java.util.List;

public interface HsCodeUseCase {
    PagedResult<HsCodeSummary> searchHsCodes(SearchHsCodeCommand command);
    HsCode getHsCodeById(Long id);
    Long createHsCode(CreateHsCodeCommand command);
    void updateHsCode(Long id, UpdateHsCodeCommand command);
    void deleteHsCode(Long id);
    void deleteHsCodes(List<Long> ids);
}
