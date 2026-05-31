package com.freightos.admin.adapter.out.persistence.subscriber;

import com.freightos.admin.application.subscriber.command.SearchSubscriberCommand;
import com.freightos.admin.application.subscriber.port.out.SubscriberPort;
import com.freightos.admin.application.subscriber.projection.SubscriberSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.subscriber.entity.Subscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriberPersistenceAdapter implements SubscriberPort {

    private final SubscriberRepository subscriberRepository;
    private final SubscriberDomainToJpaMapper domainToJpaMapper;
    private final SubscriberJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<SubscriberSummary> searchSummaries(SearchSubscriberCommand command) {
        return subscriberRepository.searchSummaries(command);
    }

    @Override
    public Optional<Subscriber> findById(Long id) {
        return subscriberRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Optional<Subscriber> findBySubscriberCode(String subscriberCode) {
        return subscriberRepository.findBySubscriberCode(subscriberCode).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Subscriber subscriber) {
        SubscriberJpaEntity entity = domainToJpaMapper.toNewJpa(subscriber);
        subscriberRepository.save(entity);
        return entity.getSubscriberId();
    }

    @Override
    public void update(Long id, Subscriber patchData) {
        SubscriberJpaEntity entity = subscriberRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("SUBSCRIBER_NOT_FOUND", MessageCode.SUBSCRIBER_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        SubscriberJpaEntity entity = subscriberRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("SUBSCRIBER_NOT_FOUND", MessageCode.SUBSCRIBER_NOT_FOUND.getMessage()));
        entity.setDeletedAt(OffsetDateTime.now());
        entity.setActive(false);
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        return subscriberRepository.autocomplete(query, limit);
    }
}
