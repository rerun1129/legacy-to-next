package com.freightos.admin.adapter.out.persistence.subscription;

import com.freightos.admin.application.auth.port.out.SubscriptionQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SubscriptionQueryPersistenceAdapter implements SubscriptionQueryPort {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public Set<String> findValidModuleCodes(Long subscriberId, LocalDate today) {
        if (subscriberId == null) {
            return Set.of();
        }
        return Set.copyOf(subscriptionRepository.findValidModuleCodes(subscriberId, today));
    }
}
