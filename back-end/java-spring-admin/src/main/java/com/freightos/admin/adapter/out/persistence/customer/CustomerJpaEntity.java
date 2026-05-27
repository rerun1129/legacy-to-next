package com.freightos.admin.adapter.out.persistence.customer;

import com.freightos.admin.common.persistence.BaseJpaEntity;
import com.freightos.admin.domain.customer.entity.CustomerType;
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
@Table(schema = "admin", name = "customer")
@Getter
@Setter
@NoArgsConstructor
public class CustomerJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    @Column(name = "customer_code", nullable = false, length = 40, updatable = false, unique = true)
    private String customerCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 30)
    private CustomerType customerType;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(name = "business_no", length = 50)
    private String businessNo;

    @Column(name = "representative", length = 100)
    private String representative;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "customer_local_address", length = 4000)
    private String customerLocalAddress;

    @Column(name = "customer_english_address", length = 4000)
    private String customerEnglishAddress;

    @Column(name = "country_code", length = 5)
    private String countryCode;

    @Column(name = "memo", length = 1000)
    private String memo;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
