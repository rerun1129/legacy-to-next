package com.freightos.admin.adapter.in.web.attributedefinition;

import com.freightos.admin.adapter.in.web.attributedefinition.dto.AttributeDefinitionDetailResponse;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.AttributeDefinitionSummaryResponse;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.CreateAttributeDefinitionRequest;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.SearchAttributeDefinitionRequest;
import com.freightos.admin.adapter.in.web.attributedefinition.dto.UpdateAttributeDefinitionRequest;
import com.freightos.admin.application.attributedefinition.command.CreateAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.command.SearchAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.command.UpdateAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.projection.AttributeDefinitionSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;
import org.springframework.stereotype.Component;

@Component
public class AttributeDefinitionAssembler {

    public SearchAttributeDefinitionCommand toSearchCommand(SearchAttributeDefinitionRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchAttributeDefinitionCommand(req.attributeKey(), req.name(), req.valueType(), req.active(), req.page(), size);
    }

    public CreateAttributeDefinitionCommand toCreateCommand(CreateAttributeDefinitionRequest req) {
        return new CreateAttributeDefinitionCommand(req.attributeKey(), req.name(), req.description(), req.valueType(), req.active());
    }

    public UpdateAttributeDefinitionCommand toUpdateCommand(UpdateAttributeDefinitionRequest req) {
        return new UpdateAttributeDefinitionCommand(req.name(), req.description(), req.valueType(), req.active());
    }

    public AttributeDefinitionSummaryResponse toSummaryResponse(AttributeDefinitionSummary p) {
        return new AttributeDefinitionSummaryResponse(p.id(), p.attributeKey(), p.name(), p.description(), p.valueType(), p.active(), p.updatedAt());
    }

    public AttributeDefinitionDetailResponse toDetail(AttributeDefinition domain) {
        return new AttributeDefinitionDetailResponse(
                domain.getId(), domain.getAttributeKey(), domain.getName(), domain.getDescription(),
                domain.getValueType().name(), domain.getActive(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<AttributeDefinitionSummaryResponse> toSummaryPage(PagedResult<AttributeDefinitionSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
