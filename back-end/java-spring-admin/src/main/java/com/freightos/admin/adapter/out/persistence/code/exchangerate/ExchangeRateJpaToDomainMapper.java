package com.freightos.admin.adapter.out.persistence.code.exchangerate;

import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRate;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateJpaToDomainMapper {

    public ExchangeRate toDomain(ExchangeRateJpaEntity e) {
        ExchangeRate domain = ExchangeRate.create(
                e.getFromCurrencyCode(), e.getToCurrencyCode(), e.getExchangeDate(),
                e.getCashSellExchangeRate(), e.getCashBuyExchangeRate(),
                e.getWireSendExchangeRate(), e.getWireReceiveExchangeRate(),
                e.getStandardExchangeRate(), e.getName(), e.getNameEn(), e.getActive()
        );
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
