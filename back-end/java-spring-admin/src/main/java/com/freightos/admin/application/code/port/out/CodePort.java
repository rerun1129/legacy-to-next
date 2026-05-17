package com.freightos.admin.application.code.port.out;

import com.freightos.admin.application.code.command.SearchCodeCommand;
import com.freightos.admin.application.code.projection.CodeSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.entity.Code;

import java.util.Optional;

public interface CodePort {
    PagedResult<CodeSummary> searchSummaries(SearchCodeCommand command);
    Optional<Code> findById(Long id);
    Long save(Code code);
    void update(Long id, Code patchData);
    void deleteById(Long id);
}
