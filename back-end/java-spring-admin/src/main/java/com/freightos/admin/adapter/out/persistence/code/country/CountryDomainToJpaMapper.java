package com.freightos.admin.adapter.out.persistence.code.country;

import com.freightos.admin.domain.code.country.entity.Country;
import org.springframework.stereotype.Component;

@Component
public class CountryDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public CountryJpaEntity toNewJpa(Country domain) {
        CountryJpaEntity entity = new CountryJpaEntity();
        entity.setCountryCode(domain.getCountryCode());
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. countryCode는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(CountryJpaEntity entity, Country patch) {
        entity.setName(patch.getName());
        entity.setNameEn(patch.getNameEn());
        entity.setActive(patch.isActive());
    }
}
