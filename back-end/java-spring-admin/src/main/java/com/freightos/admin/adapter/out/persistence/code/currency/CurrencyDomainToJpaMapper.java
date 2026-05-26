package com.freightos.admin.adapter.out.persistence.code.currency;

import com.freightos.admin.domain.code.currency.entity.Currency;
import org.springframework.stereotype.Component;

@Component
public class CurrencyDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public CurrencyJpaEntity toNewJpa(Currency domain) {
        CurrencyJpaEntity entity = new CurrencyJpaEntity();
        entity.setCurrencyCode(domain.getCurrencyCode());
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setSymbol(domain.getSymbol());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. currencyCode는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(CurrencyJpaEntity entity, Currency patch) {
        entity.setName(patch.getName());
        entity.setNameEn(patch.getNameEn());
        entity.setSymbol(patch.getSymbol());
        entity.setActive(patch.isActive());
    }
}
