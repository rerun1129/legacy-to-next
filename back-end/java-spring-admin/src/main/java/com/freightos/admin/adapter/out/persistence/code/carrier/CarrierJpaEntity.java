package com.freightos.admin.adapter.out.persistence.code.carrier;

import com.freightos.admin.common.persistence.BaseJpaEntity;
import com.freightos.admin.domain.code.carrier.entity.CarrierType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(schema = "admin", name = "carrier")
@Getter
@Setter
@NoArgsConstructor
public class CarrierJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrier_id")
    private Long id;

    @Column(name = "carrier_code", nullable = false, length = 20, updatable = false, unique = true)
    private String carrierCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier_type", nullable = false, length = 10)
    private CarrierType carrierType;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
