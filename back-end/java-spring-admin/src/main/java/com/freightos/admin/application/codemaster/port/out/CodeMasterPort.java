package com.freightos.admin.application.codemaster.port.out;

import com.freightos.admin.application.codemaster.command.SearchCodeMasterCommand;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codemaster.entity.CodeMaster;

import java.util.Optional;

public interface CodeMasterPort {
    PagedResult<CodeMasterSummary> searchSummaries(SearchCodeMasterCommand command);
    Optional<CodeMaster> findCodeMasterById(Long id);
    Long save(CodeMaster codeMaster);
    void update(Long id, CodeMaster patchData);
    void deleteCodeMasterById(Long id);
    boolean existsById(Long id);
}
