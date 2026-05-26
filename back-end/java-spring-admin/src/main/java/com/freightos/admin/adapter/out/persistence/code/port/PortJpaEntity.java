package com.freightos.admin.adapter.out.persistence.code.port;

import com.freightos.admin.common.persistence.BaseJpaEntity;
import com.freightos.admin.domain.code.port.entity.PortType;
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
@Table(schema = "admin", name = "port")
@Getter
@Setter
@NoArgsConstructor
public class PortJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "port_id")
    private Long id;

    @Column(name = "port_code", nullable = false, length = 10, updatable = false, unique = true)
    private String portCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "port_type", nullable = false, length = 10)
    private PortType portType;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
