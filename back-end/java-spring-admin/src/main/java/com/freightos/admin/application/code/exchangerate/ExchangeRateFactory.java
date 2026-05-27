package com.freightos.admin.application.code.exchangerate;

import com.freightos.admin.application.code.exchangerate.command.CreateExchangeRateCommand;
import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRate;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateFactory {

    public ExchangeRate from(CreateExchangeRateCommand command) {
        return ExchangeRate.create(
                command.fromCurrencyCode(), command.toCurrencyCode(), command.exchangeDate(),
                command.cashSellExchangeRate(), command.cashBuyExchangeRate(),
                command.wireSendExchangeRate(), command.wireReceiveExchangeRate(),
                command.standardExchangeRate(), command.name(), command.nameEn(), command.active()
        );
    }
}
