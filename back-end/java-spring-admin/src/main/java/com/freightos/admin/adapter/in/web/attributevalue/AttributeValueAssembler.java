package com.freightos.admin.adapter.in.web.attributevalue;

import com.freightos.admin.adapter.in.web.attributevalue.dto.AttributeValueDetailResponse;
import com.freightos.admin.adapter.in.web.attributevalue.dto.AttributeValueSummaryResponse;
import com.freightos.admin.adapter.in.web.attributevalue.dto.CreateAttributeValueRequest;
import com.freightos.admin.adapter.in.web.attributevalue.dto.SearchAttributeValueRequest;
import com.freightos.admin.adapter.in.web.attributevalue.dto.UpdateAttributeValueRequest;
import com.freightos.admin.application.attributevalue.command.CreateAttributeValueCommand;
import com.freightos.admin.application.attributevalue.command.SearchAttributeValueCommand;
import com.freightos.admin.application.attributevalue.command.UpdateAttributeValueCommand;
import com.freightos.admin.application.attributevalue.projection.AttributeValueSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
import org.springframework.stereotype.Component;

@Component
public class AttributeValueAssembler {

    public SearchAttributeValueCommand toSearchCommand(SearchAttributeValueRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchAttributeValueCommand(req.attributeKey(), req.value(), req.active(), req.page(), size);
    }

    public CreateAttributeValueCommand toCreateCommand(CreateAttributeValueRequest req) {
        return new CreateAttributeValueCommand(req.attributeKey(), req.value(), req.label(), req.sortOrder(), req.active());
    }

    public UpdateAttributeValueCommand toUpdateCommand(UpdateAttributeValueRequest req) {
        return new UpdateAttributeValueCommand(req.label(), req.sortOrder(), req.active());
    }

    public AttributeValueSummaryResponse toSummaryResponse(AttributeValueSummary p) {
        return new AttributeValueSummaryResponse(p.attributeKey(), p.value(), p.label(), p.sortOrder(), p.active(), p.updatedAt());
    }

    public AttributeValueDetailResponse toDetail(AttributeValue domain) {
        return new AttributeValueDetailResponse(
                domain.getAttributeKey(), domain.getValue(), domain.getLabel(),
                domain.getSortOrder(), domain.getActive(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<AttributeValueSummaryResponse> toSummaryPage(PagedResult<AttributeValueSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
