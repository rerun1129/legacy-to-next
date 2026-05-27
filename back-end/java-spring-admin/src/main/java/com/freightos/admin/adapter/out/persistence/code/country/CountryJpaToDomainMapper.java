package com.freightos.admin.adapter.out.persistence.code.country;

import com.freightos.admin.domain.code.country.entity.Country;
import org.springframework.stereotype.Component;

@Component
public class CountryJpaToDomainMapper {

    public Country toDomain(CountryJpaEntity e) {
        Country domain = Country.create(e.getCountryCode(), e.getName(), e.getNameEn(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
