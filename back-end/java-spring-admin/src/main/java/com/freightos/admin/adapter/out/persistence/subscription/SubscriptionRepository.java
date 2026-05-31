package com.freightos.admin.adapter.out.persistence.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<SubscriptionJpaEntity, Long> {

    @Query("SELECT s.moduleCode FROM SubscriptionJpaEntity s " +
           "WHERE s.subscriberId = :subscriberId " +
           "  AND s.active = true " +
           "  AND s.startDate <= :today " +
           "  AND s.endDate >= :today")
    List<String> findValidModuleCodes(@Param("subscriberId") Long subscriberId,
                                      @Param("today") LocalDate today);

    List<SubscriptionJpaEntity> findBySubscriberIdOrderByModuleCode(Long subscriberId);

    boolean existsBySubscriberIdAndModuleCode(Long subscriberId, String moduleCode);
}
