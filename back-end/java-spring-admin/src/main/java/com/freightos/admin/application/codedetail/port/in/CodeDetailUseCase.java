package com.freightos.admin.application.codedetail.port.in;

import com.freightos.admin.application.codedetail.command.CreateCodeDetailCommand;
import com.freightos.admin.application.codedetail.command.SearchCodeDetailCommand;
import com.freightos.admin.application.codedetail.command.UpdateCodeDetailCommand;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codedetail.entity.CodeDetail;

import java.util.List;

public interface CodeDetailUseCase {
    PagedResult<CodeDetailSummary> searchCodeDetails(SearchCodeDetailCommand command);
    CodeDetail findCodeDetailById(Long id);
    Long createCodeDetail(CreateCodeDetailCommand command);
    void updateCodeDetail(Long id, UpdateCodeDetailCommand command);
    void deleteCodeDetailById(Long id);
    void deleteCodeDetails(List<Long> ids);
}
