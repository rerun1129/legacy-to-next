package com.freightos.admin.application.subscription;

import com.freightos.admin.application.subscriber.port.in.SubscriberUseCase;
import com.freightos.admin.application.subscription.command.SaveSubscriptionChangesCommand;
import com.freightos.admin.application.subscription.port.in.SubscriptionUseCase;
import com.freightos.admin.application.subscription.port.out.SubscriptionPort;
import com.freightos.admin.application.subscription.projection.SubscriptionSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.subscription.entity.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService implements SubscriptionUseCase {

    private final SubscriptionPort subscriptionPort;
    private final SubscriberUseCase subscriberUseCase;

    @Override
    public List<SubscriptionSummary> getSubscriptionsBySubscriberId(Long subscriberId) {
        // 구독사 존재 여부 검증
        subscriberUseCase.getSubscriberById(subscriberId);
        return subscriptionPort.findBySubscriberId(subscriberId);
    }

    @Override
    @Transactional
    public SaveChangesResult saveSubscriptionChanges(SaveSubscriptionChangesCommand command) {
        // 구독사 존재 여부 검증
        subscriberUseCase.getSubscriberById(command.subscriberId());

        for (Long id : command.deleteIds()) {
            subscriptionPort.delete(id);
        }
        for (SaveSubscriptionChangesCommand.UpdateEntry entry : command.updates()) {
            if (entry.startDate().isAfter(entry.endDate())) {
                throw ApplicationException.badRequest("SUBSCRIPTION_DATE_RANGE_INVALID", MessageCode.SUBSCRIPTION_DATE_RANGE_INVALID.getMessage());
            }
            Subscription existing = subscriptionPort.findById(entry.id())
                    .orElseThrow(() -> ApplicationException.notFound("SUBSCRIPTION_NOT_FOUND", MessageCode.SUBSCRIPTION_NOT_FOUND.getMessage()));
            existing.applyUpdate(entry.startDate(), entry.endDate(), entry.active());
            subscriptionPort.update(entry.id(), existing);
        }
        for (SaveSubscriptionChangesCommand.CreateEntry create : command.creates()) {
            if (create.startDate().isAfter(create.endDate())) {
                throw ApplicationException.badRequest("SUBSCRIPTION_DATE_RANGE_INVALID", MessageCode.SUBSCRIPTION_DATE_RANGE_INVALID.getMessage());
            }
            if (subscriptionPort.existsBySubscriberIdAndModuleCode(command.subscriberId(), create.moduleCode())) {
                throw ApplicationException.conflict("SUBSCRIPTION_DUPLICATE_MODULE", MessageCode.SUBSCRIPTION_DUPLICATE_MODULE.getMessage());
            }
            subscriptionPort.save(Subscription.create(command.subscriberId(), create.moduleCode(), create.startDate(), create.endDate(), create.active()));
        }
        return new SaveChangesResult(command.creates().size(), command.updates().size(), command.deleteIds().size());
    }
}
