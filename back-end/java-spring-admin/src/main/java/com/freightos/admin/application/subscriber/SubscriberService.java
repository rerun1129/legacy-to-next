package com.freightos.admin.application.subscriber;

import com.freightos.admin.application.subscriber.command.CreateSubscriberCommand;
import com.freightos.admin.application.subscriber.command.SaveSubscriberChangesCommand;
import com.freightos.admin.application.subscriber.command.SearchSubscriberCommand;
import com.freightos.admin.application.subscriber.command.UpdateSubscriberCommand;
import com.freightos.admin.application.subscriber.port.in.SubscriberUseCase;
import com.freightos.admin.application.subscriber.port.out.SubscriberPort;
import com.freightos.admin.application.subscriber.projection.SubscriberSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.subscriber.entity.Subscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriberService implements SubscriberUseCase {

    private final SubscriberPort subscriberPort;

    @Override
    public PagedResult<SubscriberSummary> searchSubscribers(SearchSubscriberCommand command) {
        return subscriberPort.searchSummaries(command);
    }

    @Override
    public Subscriber getSubscriberById(Long id) {
        return subscriberPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("SUBSCRIBER_NOT_FOUND", MessageCode.SUBSCRIBER_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createSubscriber(CreateSubscriberCommand command) {
        try {
            return subscriberPort.save(Subscriber.create(
                    command.subscriberCode(), command.name(), command.nameEn(),
                    command.businessNo(), command.representative(), command.phone(),
                    command.email(), command.memo(), command.active()));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("SUBSCRIBER_DUPLICATE_CODE", MessageCode.SUBSCRIBER_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateSubscriber(Long id, UpdateSubscriberCommand command) {
        Subscriber existing = getSubscriberById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("SUBSCRIBER_ALREADY_DELETED", MessageCode.SUBSCRIBER_ALREADY_DELETED.getMessage());
        }
        existing.applyUpdate(command.name(), command.nameEn(), command.businessNo(),
                command.representative(), command.phone(), command.email(), command.memo(), command.active());
        subscriberPort.update(id, existing);
    }

    @Override
    @Transactional
    public void deleteSubscriber(Long id) {
        Subscriber existing = getSubscriberById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("SUBSCRIBER_ALREADY_DELETED", MessageCode.SUBSCRIBER_ALREADY_DELETED.getMessage());
        }
        subscriberPort.softDelete(id);
    }

    @Override
    @Transactional
    public SaveChangesResult saveSubscriberChanges(SaveSubscriberChangesCommand command) {
        for (Long id : command.deleteIds()) {
            deleteSubscriber(id);
        }
        for (SaveSubscriberChangesCommand.UpdateEntry entry : command.updates()) {
            updateSubscriber(entry.id(), entry.command());
        }
        for (CreateSubscriberCommand create : command.creates()) {
            createSubscriber(create);
        }
        return new SaveChangesResult(command.creates().size(), command.updates().size(), command.deleteIds().size());
    }

    @Override
    public List<AutocompleteItem> autocompleteSubscribers(String query, int limit) {
        return subscriberPort.autocomplete(query, limit);
    }
}
