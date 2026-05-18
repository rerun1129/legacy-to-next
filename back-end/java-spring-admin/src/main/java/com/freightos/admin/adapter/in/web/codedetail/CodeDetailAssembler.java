package com.freightos.admin.adapter.in.web.codedetail;

import com.freightos.admin.adapter.in.web.codedetail.dto.CodeDetailDetailResponse;
import com.freightos.admin.adapter.in.web.codedetail.dto.CodeDetailSummaryResponse;
import com.freightos.admin.adapter.in.web.codedetail.dto.CreateCodeDetailRequest;
import com.freightos.admin.adapter.in.web.codedetail.dto.SearchCodeDetailRequest;
import com.freightos.admin.adapter.in.web.codedetail.dto.UpdateCodeDetailRequest;
import com.freightos.admin.application.codedetail.command.CreateCodeDetailCommand;
import com.freightos.admin.application.codedetail.command.SearchCodeDetailCommand;
import com.freightos.admin.application.codedetail.command.UpdateCodeDetailCommand;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codedetail.entity.CodeDetail;
import org.springframework.stereotype.Component;

@Component
public class CodeDetailAssembler {

    public SearchCodeDetailCommand toSearchCommand(SearchCodeDetailRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchCodeDetailCommand(req.masterId(), req.codeValue(), req.codeLabel(), req.active(), req.page(), size);
    }

    public CreateCodeDetailCommand toCreateCommand(CreateCodeDetailRequest req) {
        return new CreateCodeDetailCommand(req.masterId(), req.codeValue(), req.codeLabel(), req.sortOrder(), req.active(), req.remark());
    }

    public UpdateCodeDetailCommand toUpdateCommand(UpdateCodeDetailRequest req) {
        return new UpdateCodeDetailCommand(req.codeLabel(), req.sortOrder(), req.active(), req.remark());
    }

    public CodeDetailSummaryResponse toSummaryResponse(CodeDetailSummary p) {
        return new CodeDetailSummaryResponse(p.id(), p.masterId(), p.codeValue(), p.codeLabel(), p.sortOrder(), p.active(), p.updatedAt());
    }

    public CodeDetailDetailResponse toDetail(CodeDetail domain) {
        return new CodeDetailDetailResponse(
                domain.getId(), domain.getMasterId(), domain.getCodeValue(), domain.getCodeLabel(),
                domain.getSortOrder(), domain.getActive(), domain.getRemark(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<CodeDetailSummaryResponse> toSummaryPage(PagedResult<CodeDetailSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
