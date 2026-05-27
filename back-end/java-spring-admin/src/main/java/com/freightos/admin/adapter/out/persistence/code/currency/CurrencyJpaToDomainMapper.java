package com.freightos.admin.adapter.out.persistence.code.currency;

import com.freightos.admin.domain.code.currency.entity.Currency;
import org.springframework.stereotype.Component;

@Component
public class CurrencyJpaToDomainMapper {

    public Currency toDomain(CurrencyJpaEntity e) {
        Currency domain = Currency.create(e.getCurrencyCode(), e.getName(), e.getNameEn(), e.getSymbol(), e.getCurrencyUnit(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
