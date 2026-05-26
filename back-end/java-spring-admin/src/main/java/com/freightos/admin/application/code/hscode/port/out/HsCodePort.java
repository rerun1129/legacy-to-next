package com.freightos.admin.application.code.hscode.port.out;

import com.freightos.admin.application.code.hscode.command.SearchHsCodeCommand;
import com.freightos.admin.application.code.hscode.projection.HsCodeSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.hscode.entity.HsCode;

import java.util.Optional;

public interface HsCodePort {
    PagedResult<HsCodeSummary> searchSummaries(SearchHsCodeCommand command);
    Optional<HsCode> findById(Long id);
    Long save(HsCode hsCode);
    void update(Long id, HsCode patchData);
    void softDelete(Long id);
}
