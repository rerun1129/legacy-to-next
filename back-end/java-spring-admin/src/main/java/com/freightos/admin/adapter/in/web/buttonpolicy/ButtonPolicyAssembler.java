package com.freightos.admin.adapter.in.web.buttonpolicy;

import com.freightos.admin.adapter.in.web.buttonpolicy.dto.ButtonPolicyDetailResponse;
import com.freightos.admin.adapter.in.web.buttonpolicy.dto.ButtonPolicySummaryResponse;
import com.freightos.admin.adapter.in.web.buttonpolicy.dto.CreateButtonPolicyRequest;
import com.freightos.admin.adapter.in.web.buttonpolicy.dto.SearchButtonPolicyRequest;
import com.freightos.admin.application.buttonpolicy.command.CreateButtonPolicyCommand;
import com.freightos.admin.application.buttonpolicy.command.SearchButtonPolicyCommand;
import com.freightos.admin.application.buttonpolicy.projection.ButtonPolicySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.buttonpolicy.entity.ButtonPolicy;
import org.springframework.stereotype.Component;

@Component
public class ButtonPolicyAssembler {

    public SearchButtonPolicyCommand toSearchCommand(SearchButtonPolicyRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchButtonPolicyCommand(req.buttonId(), req.attributeKey(), req.requiredValue(), req.page(), size);
    }

    public CreateButtonPolicyCommand toCreateCommand(CreateButtonPolicyRequest req) {
        return new CreateButtonPolicyCommand(req.buttonId(), req.attributeKey(), req.requiredValue());
    }

    public ButtonPolicySummaryResponse toSummaryResponse(ButtonPolicySummary p) {
        return new ButtonPolicySummaryResponse(p.id(), p.buttonId(), p.attributeKey(), p.requiredValue(), p.updatedAt());
    }

    public ButtonPolicyDetailResponse toDetail(ButtonPolicy domain) {
        return new ButtonPolicyDetailResponse(
                domain.getId(), domain.getButtonId(), domain.getAttributeKey(), domain.getRequiredValue(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<ButtonPolicySummaryResponse> toSummaryPage(PagedResult<ButtonPolicySummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
