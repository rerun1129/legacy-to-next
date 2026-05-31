package com.freightos.admin.adapter.out.persistence.subscription;

import com.freightos.admin.application.subscription.port.out.SubscriptionPort;
import com.freightos.admin.application.subscription.projection.SubscriptionSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.domain.subscription.entity.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionPersistenceAdapter implements SubscriptionPort {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionDomainToJpaMapper domainToJpaMapper;
    private final SubscriptionJpaToDomainMapper jpaToDomainMapper;

    @Override
    public List<SubscriptionSummary> findBySubscriberId(Long subscriberId) {
        return subscriptionRepository.findBySubscriberIdOrderByModuleCode(subscriberId).stream()
                .map(jpaToDomainMapper::toSummary)
                .toList();
    }

    @Override
    public Optional<Subscription> findById(Long id) {
        return subscriptionRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public boolean existsBySubscriberIdAndModuleCode(Long subscriberId, String moduleCode) {
        return subscriptionRepository.existsBySubscriberIdAndModuleCode(subscriberId, moduleCode);
    }

    @Override
    public Long save(Subscription subscription) {
        SubscriptionJpaEntity entity = domainToJpaMapper.toNewJpa(subscription);
        subscriptionRepository.save(entity);
        return entity.getSubscriptionId();
    }

    @Override
    public void update(Long id, Subscription patchData) {
        SubscriptionJpaEntity entity = subscriptionRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("SUBSCRIPTION_NOT_FOUND", MessageCode.SUBSCRIPTION_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void delete(Long id) {
        subscriptionRepository.deleteById(id);
    }
}
