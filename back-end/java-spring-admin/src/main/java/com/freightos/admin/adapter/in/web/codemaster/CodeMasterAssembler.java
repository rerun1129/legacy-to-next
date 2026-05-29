package com.freightos.admin.adapter.in.web.codemaster;

import com.freightos.admin.adapter.in.web.codemaster.dto.CodeMasterDetailResponse;
import com.freightos.admin.adapter.in.web.codemaster.dto.CodeMasterSummaryResponse;
import com.freightos.admin.adapter.in.web.codemaster.dto.CreateCodeMasterRequest;
import com.freightos.admin.adapter.in.web.codemaster.dto.SaveCodeMasterChangesRequest;
import com.freightos.admin.adapter.in.web.codemaster.dto.SearchCodeMasterRequest;
import com.freightos.admin.adapter.in.web.codemaster.dto.UpdateCodeMasterRequest;
import com.freightos.admin.application.codemaster.command.CreateCodeMasterCommand;
import com.freightos.admin.application.codemaster.command.SaveCodeMasterChangesCommand;
import com.freightos.admin.application.codemaster.command.SearchCodeMasterCommand;
import com.freightos.admin.application.codemaster.command.UpdateCodeMasterCommand;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codemaster.entity.CodeMaster;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CodeMasterAssembler {

    public SearchCodeMasterCommand toSearchCommand(SearchCodeMasterRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchCodeMasterCommand(req.masterCode(), req.masterName(), req.active(), req.page(), size);
    }

    public CreateCodeMasterCommand toCreateCommand(CreateCodeMasterRequest req) {
        return new CreateCodeMasterCommand(req.masterCode(), req.masterName(), req.description(), req.sortOrder(), req.active());
    }

    public UpdateCodeMasterCommand toUpdateCommand(UpdateCodeMasterRequest req) {
        return new UpdateCodeMasterCommand(req.masterName(), req.description(), req.sortOrder(), req.active());
    }

    public CodeMasterSummaryResponse toSummaryResponse(CodeMasterSummary p) {
        return new CodeMasterSummaryResponse(p.id(), p.masterCode(), p.masterName(), p.description(), p.sortOrder(), p.active(), p.updatedAt());
    }

    public CodeMasterDetailResponse toDetail(CodeMaster domain) {
        return new CodeMasterDetailResponse(
                domain.getId(), domain.getMasterCode(), domain.getMasterName(), domain.getDescription(),
                domain.getSortOrder(), domain.getActive(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<CodeMasterSummaryResponse> toSummaryPage(PagedResult<CodeMasterSummary> src) {
        return src.map(this::toSummaryResponse);
    }

    public SaveCodeMasterChangesCommand toSaveChangesCommand(SaveCodeMasterChangesRequest req) {
        List<CreateCodeMasterCommand> creates = req.creates() == null ? List.of()
                : req.creates().stream().map(this::toCreateCommand).toList();
        List<SaveCodeMasterChangesCommand.UpdateEntry> updates = req.updates() == null ? List.of()
                : req.updates().stream()
                        .map(u -> new SaveCodeMasterChangesCommand.UpdateEntry(u.id(), new UpdateCodeMasterCommand(u.masterName(), u.description(), u.sortOrder(), u.active())))
                        .toList();
        List<Long> deleteIds = req.deleteIds() == null ? List.of() : req.deleteIds();
        return new SaveCodeMasterChangesCommand(creates, updates, deleteIds);
    }
}
