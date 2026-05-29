package com.freightos.admin.application.codemaster.port.in;

import com.freightos.admin.application.codemaster.command.CreateCodeMasterCommand;
import com.freightos.admin.application.codemaster.command.SaveCodeMasterChangesCommand;
import com.freightos.admin.application.codemaster.command.SearchCodeMasterCommand;
import com.freightos.admin.application.codemaster.command.UpdateCodeMasterCommand;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.codemaster.entity.CodeMaster;

import java.util.List;

public interface CodeMasterUseCase {
    PagedResult<CodeMasterSummary> searchCodeMasters(SearchCodeMasterCommand command);
    CodeMaster findCodeMasterById(Long id);
    Long createCodeMaster(CreateCodeMasterCommand command);
    void updateCodeMaster(Long id, UpdateCodeMasterCommand command);
    void deleteCodeMasterById(Long id);
    void deleteCodeMasters(List<Long> ids);
    SaveChangesResult saveCodeMasterChanges(SaveCodeMasterChangesCommand command);
    List<AutocompleteItem> autocompleteCodeMasters(String query, int limit);
}
