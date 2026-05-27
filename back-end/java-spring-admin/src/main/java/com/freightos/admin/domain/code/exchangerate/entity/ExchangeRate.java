package com.freightos.admin.domain.code.exchangerate.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ExchangeRate extends BaseEntity {

    private final String fromCurrencyCode;
    private final String toCurrencyCode;
    private final String exchangeDate;
    private BigDecimal cashSellExchangeRate;
    private BigDecimal cashBuyExchangeRate;
    private BigDecimal wireSendExchangeRate;
    private BigDecimal wireReceiveExchangeRate;
    private BigDecimal standardExchangeRate;
    private String name;
    private String nameEn;
    private boolean active;
    private LocalDateTime deletedAt;

    private ExchangeRate(String fromCurrencyCode, String toCurrencyCode, String exchangeDate,
                         BigDecimal cashSellExchangeRate, BigDecimal cashBuyExchangeRate,
                         BigDecimal wireSendExchangeRate, BigDecimal wireReceiveExchangeRate,
                         BigDecimal standardExchangeRate, String name, String nameEn, boolean active) {
        this.fromCurrencyCode      = fromCurrencyCode;
        this.toCurrencyCode        = toCurrencyCode;
        this.exchangeDate          = exchangeDate;
        this.cashSellExchangeRate  = cashSellExchangeRate;
        this.cashBuyExchangeRate   = cashBuyExchangeRate;
        this.wireSendExchangeRate  = wireSendExchangeRate;
        this.wireReceiveExchangeRate = wireReceiveExchangeRate;
        this.standardExchangeRate  = standardExchangeRate;
        this.name                  = name;
        this.nameEn                = nameEn;
        this.active                = active;
        this.deletedAt             = null;
    }

    public static ExchangeRate create(String fromCurrencyCode, String toCurrencyCode, String exchangeDate,
                                      BigDecimal cashSellExchangeRate, BigDecimal cashBuyExchangeRate,
                                      BigDecimal wireSendExchangeRate, BigDecimal wireReceiveExchangeRate,
                                      BigDecimal standardExchangeRate, String name, String nameEn, boolean active) {
        return new ExchangeRate(fromCurrencyCode, toCurrencyCode, exchangeDate,
                cashSellExchangeRate, cashBuyExchangeRate, wireSendExchangeRate, wireReceiveExchangeRate,
                standardExchangeRate, name, nameEn, active);
    }

    /**
     * 수정 가능한 필드만 갱신. 식별 필드(fromCurrencyCode, toCurrencyCode, exchangeDate)는 변경 불가.
     */
    public void applyUpdate(BigDecimal cashSellExchangeRate, BigDecimal cashBuyExchangeRate,
                            BigDecimal wireSendExchangeRate, BigDecimal wireReceiveExchangeRate,
                            BigDecimal standardExchangeRate, String name, String nameEn, boolean active) {
        this.cashSellExchangeRate    = cashSellExchangeRate;
        this.cashBuyExchangeRate     = cashBuyExchangeRate;
        this.wireSendExchangeRate    = wireSendExchangeRate;
        this.wireReceiveExchangeRate = wireReceiveExchangeRate;
        this.standardExchangeRate    = standardExchangeRate;
        this.name                    = name;
        this.nameEn                  = nameEn;
        this.active                  = active;
    }

    /** soft delete: 삭제 시각 기록 + 비활성화. */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.active    = false;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 어댑터 계층이 JPA→Domain 변환 시 deletedAt을 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
