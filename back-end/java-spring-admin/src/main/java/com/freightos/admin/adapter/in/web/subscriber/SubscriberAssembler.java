package com.freightos.admin.adapter.in.web.subscriber;

import com.freightos.admin.adapter.in.web.subscriber.dto.CreateSubscriberRequest;
import com.freightos.admin.adapter.in.web.subscriber.dto.SaveSubscriberChangesRequest;
import com.freightos.admin.adapter.in.web.subscriber.dto.SearchSubscriberRequest;
import com.freightos.admin.adapter.in.web.subscriber.dto.SubscriberDetailResponse;
import com.freightos.admin.adapter.in.web.subscriber.dto.SubscriberSummaryResponse;
import com.freightos.admin.adapter.in.web.subscriber.dto.UpdateSubscriberRequest;
import com.freightos.admin.application.subscriber.command.CreateSubscriberCommand;
import com.freightos.admin.application.subscriber.command.SaveSubscriberChangesCommand;
import com.freightos.admin.application.subscriber.command.SearchSubscriberCommand;
import com.freightos.admin.application.subscriber.command.UpdateSubscriberCommand;
import com.freightos.admin.application.subscriber.projection.SubscriberSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.subscriber.entity.Subscriber;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubscriberAssembler {

    public SearchSubscriberCommand toSearchCommand(SearchSubscriberRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        String scope = req.scope() != null ? req.scope() : "ALL";
        return new SearchSubscriberCommand(req.subscriberCode(), req.name(), scope, req.page(), size);
    }

    public CreateSubscriberCommand toCreateCommand(CreateSubscriberRequest req) {
        return new CreateSubscriberCommand(req.subscriberCode(), req.name(), req.nameEn(),
                req.businessNo(), req.representative(), req.phone(), req.email(), req.memo(), req.active());
    }

    public UpdateSubscriberCommand toUpdateCommand(UpdateSubscriberRequest req) {
        return new UpdateSubscriberCommand(req.name(), req.nameEn(), req.businessNo(),
                req.representative(), req.phone(), req.email(), req.memo(), req.active());
    }

    public SubscriberSummaryResponse toSummaryResponse(SubscriberSummary p) {
        return new SubscriberSummaryResponse(p.id(), p.subscriberCode(), p.name(), p.nameEn(),
                p.businessNo(), p.representative(), p.phone(), p.email(), p.memo(),
                p.active(), p.deletedAt(), p.updatedAt());
    }

    public SubscriberDetailResponse toDetail(Subscriber domain) {
        return new SubscriberDetailResponse(domain.getId(), domain.getSubscriberCode(), domain.getName(),
                domain.getNameEn(), domain.getBusinessNo(), domain.getRepresentative(), domain.getPhone(),
                domain.getEmail(), domain.getMemo(), domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy());
    }

    public PagedResult<SubscriberSummaryResponse> toSummaryPage(PagedResult<SubscriberSummary> src) {
        return src.map(this::toSummaryResponse);
    }

    public SaveSubscriberChangesCommand toSaveChangesCommand(SaveSubscriberChangesRequest req) {
        List<CreateSubscriberCommand> creates = req.creates() == null ? List.of()
                : req.creates().stream().map(this::toCreateCommand).toList();
        List<SaveSubscriberChangesCommand.UpdateEntry> updates = req.updates() == null ? List.of()
                : req.updates().stream()
                        .map(u -> new SaveSubscriberChangesCommand.UpdateEntry(u.id(),
                            new UpdateSubscriberCommand(u.name(), u.nameEn(), u.businessNo(),
                                    u.representative(), u.phone(), u.email(), u.memo(), u.active())))
                        .toList();
        List<Long> deleteIds = req.deleteIds() == null ? List.of() : req.deleteIds();
        return new SaveSubscriberChangesCommand(creates, updates, deleteIds);
    }
}
