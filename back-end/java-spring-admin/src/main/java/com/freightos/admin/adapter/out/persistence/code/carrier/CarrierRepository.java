package com.freightos.admin.adapter.out.persistence.code.carrier;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrierRepository extends JpaRepository<CarrierJpaEntity, Long>, CarrierRepositoryCustom {
}
