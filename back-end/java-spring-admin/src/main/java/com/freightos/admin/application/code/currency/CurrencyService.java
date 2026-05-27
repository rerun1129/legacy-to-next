package com.freightos.admin.application.code.currency;

import com.freightos.admin.application.code.currency.command.CreateCurrencyCommand;
import com.freightos.admin.application.code.currency.command.SearchCurrencyCommand;
import com.freightos.admin.application.code.currency.command.UpdateCurrencyCommand;
import com.freightos.admin.application.code.currency.port.in.CurrencyUseCase;
import com.freightos.admin.application.code.currency.port.out.CurrencyPort;
import com.freightos.admin.application.code.currency.projection.CurrencySummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.currency.entity.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrencyService implements CurrencyUseCase {

    private final CurrencyPort currencyPort;
    private final CurrencyFactory currencyFactory;

    @Override
    public PagedResult<CurrencySummary> searchCurrencies(SearchCurrencyCommand command) {
        return currencyPort.searchSummaries(command);
    }

    @Override
    public Currency getCurrencyById(Long id) {
        return currencyPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CURRENCY_NOT_FOUND", MessageCode.CURRENCY_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createCurrency(CreateCurrencyCommand command) {
        try {
            return currencyPort.save(currencyFactory.from(command));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("CURRENCY_DUPLICATE_CODE", MessageCode.CURRENCY_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateCurrency(Long id, UpdateCurrencyCommand command) {
        Currency currency = getCurrencyById(id);
        if (currency.isDeleted()) {
            throw ApplicationException.conflict("CURRENCY_ALREADY_DELETED", MessageCode.CURRENCY_ALREADY_DELETED.getMessage());
        }
        currency.applyUpdate(command.name(), command.nameEn(), command.symbol(), command.currencyUnit(), command.active());
        currencyPort.update(id, currency);
    }

    @Override
    @Transactional
    public void deleteCurrency(Long id) {
        Currency currency = getCurrencyById(id);
        if (currency.isDeleted()) {
            throw ApplicationException.conflict("CURRENCY_ALREADY_DELETED", MessageCode.CURRENCY_ALREADY_DELETED.getMessage());
        }
        currencyPort.softDelete(id);
    }

    @Override
    @Transactional
    public void deleteCurrencies(List<Long> ids) {
        for (Long id : ids) {
            deleteCurrency(id);
        }
    }
}
