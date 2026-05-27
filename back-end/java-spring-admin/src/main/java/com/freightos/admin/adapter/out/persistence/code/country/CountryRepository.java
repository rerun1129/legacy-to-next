package com.freightos.admin.adapter.out.persistence.code.country;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<CountryJpaEntity, Long>, CountryRepositoryCustom {
}
