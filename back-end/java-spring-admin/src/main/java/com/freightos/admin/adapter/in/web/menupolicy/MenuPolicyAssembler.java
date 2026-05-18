package com.freightos.admin.adapter.in.web.menupolicy;

import com.freightos.admin.adapter.in.web.menupolicy.dto.CreateMenuPolicyRequest;
import com.freightos.admin.adapter.in.web.menupolicy.dto.MenuPolicyDetailResponse;
import com.freightos.admin.adapter.in.web.menupolicy.dto.MenuPolicySummaryResponse;
import com.freightos.admin.adapter.in.web.menupolicy.dto.SearchMenuPolicyRequest;
import com.freightos.admin.application.menupolicy.command.CreateMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.command.SearchMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.projection.MenuPolicySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.menupolicy.entity.MenuPolicy;
import org.springframework.stereotype.Component;

@Component
public class MenuPolicyAssembler {

    public SearchMenuPolicyCommand toSearchCommand(SearchMenuPolicyRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchMenuPolicyCommand(req.menuId(), req.attributeKey(), req.requiredValue(), req.page(), size);
    }

    public CreateMenuPolicyCommand toCreateCommand(CreateMenuPolicyRequest req) {
        return new CreateMenuPolicyCommand(req.menuId(), req.attributeKey(), req.requiredValue());
    }

    public MenuPolicySummaryResponse toSummaryResponse(MenuPolicySummary p) {
        return new MenuPolicySummaryResponse(p.id(), p.menuId(), p.attributeKey(), p.requiredValue(), p.updatedAt());
    }

    public MenuPolicyDetailResponse toDetail(MenuPolicy domain) {
        return new MenuPolicyDetailResponse(
                domain.getId(), domain.getMenuId(), domain.getAttributeKey(), domain.getRequiredValue(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<MenuPolicySummaryResponse> toSummaryPage(PagedResult<MenuPolicySummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
