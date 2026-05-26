package com.freightos.admin.adapter.out.persistence.code.exchangerate;

import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRate;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public ExchangeRateJpaEntity toNewJpa(ExchangeRate domain) {
        ExchangeRateJpaEntity entity = new ExchangeRateJpaEntity();
        entity.setBaseCurrency(domain.getBaseCurrency());
        entity.setTargetCurrency(domain.getTargetCurrency());
        entity.setRate(domain.getRate());
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. baseCurrency, targetCurrency는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(ExchangeRateJpaEntity entity, ExchangeRate patch) {
        entity.setRate(patch.getRate());
        entity.setName(patch.getName());
        entity.setNameEn(patch.getNameEn());
        entity.setActive(patch.isActive());
    }
}
