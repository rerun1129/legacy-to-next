package com.freightos.admin.application.code.currency;

import com.freightos.admin.application.code.currency.command.CreateCurrencyCommand;
import com.freightos.admin.domain.code.currency.entity.Currency;
import org.springframework.stereotype.Component;

@Component
public class CurrencyFactory {

    public Currency from(CreateCurrencyCommand command) {
        return Currency.create(command.currencyCode(), command.name(), command.nameEn(), command.symbol(), command.active());
    }
}
