package com.freightos.admin.adapter.in.web.code;

import com.freightos.admin.adapter.in.web.code.dto.CodeDetailResponse;
import com.freightos.admin.adapter.in.web.code.dto.CodeSummaryResponse;
import com.freightos.admin.adapter.in.web.code.dto.CreateCodeRequest;
import com.freightos.admin.adapter.in.web.code.dto.SearchCodeRequest;
import com.freightos.admin.adapter.in.web.code.dto.UpdateCodeRequest;
import com.freightos.admin.application.code.command.CreateCodeCommand;
import com.freightos.admin.application.code.command.SearchCodeCommand;
import com.freightos.admin.application.code.command.UpdateCodeCommand;
import com.freightos.admin.application.code.projection.CodeSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.entity.Code;
import org.springframework.stereotype.Component;

@Component
public class CodeAssembler {

    public SearchCodeCommand toSearchCommand(SearchCodeRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchCodeCommand(req.codeGroup(), req.codeValue(), req.codeLabel(), req.active(), req.page(), size);
    }

    public CreateCodeCommand toCreateCommand(CreateCodeRequest req) {
        return new CreateCodeCommand(req.codeGroup(), req.codeValue(), req.codeLabel(), req.sortOrder(), req.active(), req.remark());
    }

    public UpdateCodeCommand toUpdateCommand(UpdateCodeRequest req) {
        return new UpdateCodeCommand(req.codeLabel(), req.sortOrder(), req.active(), req.remark());
    }

    public CodeSummaryResponse toSummaryResponse(CodeSummary p) {
        return new CodeSummaryResponse(p.id(), p.codeGroup(), p.codeValue(), p.codeLabel(), p.sortOrder(), p.active(), p.updatedAt());
    }

    public CodeDetailResponse toDetail(Code domain) {
        return new CodeDetailResponse(
                domain.getId(), domain.getCodeGroup(), domain.getCodeValue(), domain.getCodeLabel(),
                domain.getSortOrder(), domain.getActive(), domain.getRemark(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<CodeSummaryResponse> toSummaryPage(PagedResult<CodeSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
