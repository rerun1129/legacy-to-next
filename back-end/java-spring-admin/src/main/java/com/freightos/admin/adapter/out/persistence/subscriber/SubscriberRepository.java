package com.freightos.admin.adapter.out.persistence.subscriber;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<SubscriberJpaEntity, Long>, SubscriberRepositoryCustom {

    Optional<SubscriberJpaEntity> findBySubscriberCode(String subscriberCode);
}
