package com.freightos.admin.application.codedetail.port.out;

import com.freightos.admin.application.codedetail.command.SearchCodeDetailCommand;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codedetail.entity.CodeDetail;

import java.util.Optional;

public interface CodeDetailPort {
    PagedResult<CodeDetailSummary> searchSummaries(SearchCodeDetailCommand command);
    Optional<CodeDetail> findCodeDetailById(Long id);
    Long save(CodeDetail codeDetail);
    void update(Long id, CodeDetail patchData);
    void deleteCodeDetailById(Long id);
    long countByMasterId(Long masterId);
}
