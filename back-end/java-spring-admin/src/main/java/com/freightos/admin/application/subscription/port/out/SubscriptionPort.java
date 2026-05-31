package com.freightos.admin.application.subscription.port.out;

import com.freightos.admin.application.subscription.projection.SubscriptionSummary;
import com.freightos.admin.domain.subscription.entity.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPort {
    List<SubscriptionSummary> findBySubscriberId(Long subscriberId);
    Optional<Subscription> findById(Long id);
    boolean existsBySubscriberIdAndModuleCode(Long subscriberId, String moduleCode);
    Long save(Subscription subscription);
    void update(Long id, Subscription patchData);
    void delete(Long id);
}
