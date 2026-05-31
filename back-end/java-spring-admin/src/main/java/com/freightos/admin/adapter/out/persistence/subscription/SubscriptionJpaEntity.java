package com.freightos.admin.adapter.out.persistence.subscription;

import com.freightos.admin.common.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(schema = "admin", name = "subscription")
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    /** FK → admin.subscriber.subscriber_id (Plain Long, @ManyToOne 미사용) */
    @Column(name = "subscriber_id", nullable = false, updatable = false)
    private Long subscriberId;

    /** module 값 (admin.attribute_value attribute_key='module'; FK 없음, Plain String, @ManyToOne 미사용) */
    @Column(name = "module_code", nullable = false, length = 40, updatable = false)
    private String moduleCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
