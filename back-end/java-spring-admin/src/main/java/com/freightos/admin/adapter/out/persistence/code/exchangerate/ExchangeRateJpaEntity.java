package com.freightos.admin.adapter.out.persistence.code.exchangerate;

import com.freightos.admin.common.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(schema = "admin", name = "exchange_rate",
        uniqueConstraints = @UniqueConstraint(columnNames = {"from_currency_code", "to_currency_code", "exchange_date"}))
@Getter
@Setter
@NoArgsConstructor
public class ExchangeRateJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exchange_rate_id")
    private Long id;

    @Column(name = "from_currency_code", nullable = false, length = 3, updatable = false)
    private String fromCurrencyCode;

    @Column(name = "to_currency_code", nullable = false, length = 3, updatable = false)
    private String toCurrencyCode;

    @Column(name = "exchange_date", length = 8, updatable = false)
    private String exchangeDate;

    @Column(name = "cash_sell_exchange_rate", precision = 10, scale = 4)
    private BigDecimal cashSellExchangeRate;

    @Column(name = "cash_buy_exchange_rate", precision = 10, scale = 4)
    private BigDecimal cashBuyExchangeRate;

    @Column(name = "wire_send_exchange_rate", precision = 10, scale = 4)
    private BigDecimal wireSendExchangeRate;

    @Column(name = "wire_receive_exchange_rate", precision = 10, scale = 4)
    private BigDecimal wireReceiveExchangeRate;

    @Column(name = "standard_exchange_rate", precision = 10, scale = 4)
    private BigDecimal standardExchangeRate;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
