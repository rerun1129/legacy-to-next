package com.freightos.admin.adapter.in.web.button;

import com.freightos.admin.adapter.in.web.button.dto.ButtonDetailResponse;
import com.freightos.admin.adapter.in.web.button.dto.ButtonSummaryResponse;
import com.freightos.admin.adapter.in.web.button.dto.CreateButtonRequest;
import com.freightos.admin.adapter.in.web.button.dto.SearchButtonRequest;
import com.freightos.admin.adapter.in.web.button.dto.UpdateButtonRequest;
import com.freightos.admin.application.button.command.CreateButtonCommand;
import com.freightos.admin.application.button.command.SearchButtonCommand;
import com.freightos.admin.application.button.command.UpdateButtonCommand;
import com.freightos.admin.application.button.projection.ButtonSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.button.entity.Button;
import org.springframework.stereotype.Component;

@Component
public class ButtonAssembler {

    public SearchButtonCommand toSearchCommand(SearchButtonRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchButtonCommand(req.menuId(), req.buttonCode(), req.label(), req.actionType(), req.active(), req.page(), size);
    }

    public CreateButtonCommand toCreateCommand(CreateButtonRequest req) {
        return new CreateButtonCommand(req.buttonCode(), req.menuId(), req.label(), req.actionType(), req.apiMethod(), req.apiPath(), req.sortOrder(), req.active());
    }

    public UpdateButtonCommand toUpdateCommand(UpdateButtonRequest req) {
        return new UpdateButtonCommand(req.menuId(), req.label(), req.actionType(), req.apiMethod(), req.apiPath(), req.sortOrder(), req.active());
    }

    public ButtonSummaryResponse toSummaryResponse(ButtonSummary p) {
        return new ButtonSummaryResponse(p.id(), p.buttonCode(), p.menuId(), p.label(), p.actionType(), p.apiMethod(), p.apiPath(), p.sortOrder(), p.active(), p.updatedAt());
    }

    public ButtonDetailResponse toDetail(Button domain) {
        return new ButtonDetailResponse(
                domain.getId(), domain.getButtonCode(), domain.getMenuId(), domain.getLabel(),
                domain.getActionType().name(), domain.getApiMethod(), domain.getApiPath(),
                domain.getSortOrder(), domain.getActive(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<ButtonSummaryResponse> toSummaryPage(PagedResult<ButtonSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
