package com.freightos.admin.application.code.exchangerate;

import com.freightos.admin.application.code.exchangerate.command.CreateExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.command.SaveExchangeRateChangesCommand;
import com.freightos.admin.application.code.exchangerate.command.SearchExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.command.UpdateExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.port.in.ExchangeRateUseCase;
import com.freightos.admin.application.code.exchangerate.port.out.ExchangeRatePort;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateValue;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRate;
import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRateKind;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExchangeRateService implements ExchangeRateUseCase {

    private final ExchangeRatePort exchangeRatePort;
    private final ExchangeRateFactory exchangeRateFactory;

    @Override
    public PagedResult<ExchangeRateSummary> searchExchangeRates(SearchExchangeRateCommand command) {
        return exchangeRatePort.searchSummaries(command);
    }

    @Override
    public ExchangeRate getExchangeRateById(Long id) {
        return exchangeRatePort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("EXCHANGE_RATE_NOT_FOUND", MessageCode.EXCHANGE_RATE_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createExchangeRate(CreateExchangeRateCommand command) {
        try {
            return exchangeRatePort.save(exchangeRateFactory.from(command));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("EXCHANGE_RATE_DUPLICATE_CODE", MessageCode.EXCHANGE_RATE_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateExchangeRate(Long id, UpdateExchangeRateCommand command) {
        ExchangeRate exchangeRate = getExchangeRateById(id);
        if (exchangeRate.isDeleted()) {
            throw ApplicationException.conflict("EXCHANGE_RATE_ALREADY_DELETED", MessageCode.EXCHANGE_RATE_ALREADY_DELETED.getMessage());
        }
        exchangeRate.applyUpdate(command.cashSellExchangeRate(), command.cashBuyExchangeRate(),
                command.wireSendExchangeRate(), command.wireReceiveExchangeRate(),
                command.standardExchangeRate(), command.name(), command.nameEn(), command.active());
        exchangeRatePort.update(id, exchangeRate);
    }

    @Override
    @Transactional
    public void deleteExchangeRate(Long id) {
        ExchangeRate exchangeRate = getExchangeRateById(id);
        if (exchangeRate.isDeleted()) {
            throw ApplicationException.conflict("EXCHANGE_RATE_ALREADY_DELETED", MessageCode.EXCHANGE_RATE_ALREADY_DELETED.getMessage());
        }
        exchangeRatePort.softDelete(id);
    }

    @Override
    @Transactional
    public void deleteExchangeRates(List<Long> ids) {
        for (Long id : ids) {
            deleteExchangeRate(id);
        }
    }

    @Override
    @Transactional
    public SaveChangesResult saveExchangeRateChanges(SaveExchangeRateChangesCommand command) {
        for (Long id : command.deleteIds()) {
            deleteExchangeRate(id);
        }
        for (SaveExchangeRateChangesCommand.UpdateEntry entry : command.updates()) {
            updateExchangeRate(entry.id(), entry.command());
        }
        for (CreateExchangeRateCommand create : command.creates()) {
            createExchangeRate(create);
        }
        return new SaveChangesResult(
                command.creates().size(),
                command.updates().size(),
                command.deleteIds().size()
        );
    }

    @Override
    public List<AutocompleteItem> autocompleteExchangeRates(String query, int limit) {
        return exchangeRatePort.autocomplete(query, limit);
    }

    @Override
    public List<ExchangeRateValue> findRates(String fromCurrencyCode, String toCurrencyCode, String exchangeDate) {
        return exchangeRatePort.findActiveByDateCurrency(fromCurrencyCode, toCurrencyCode, exchangeDate)
                .map(this::toValueList)
                .orElse(List.of());
    }

    private List<ExchangeRateValue> toValueList(ExchangeRate domain) {
        List<ExchangeRateValue> result = new ArrayList<>();
        addIfPresent(result, ExchangeRateKind.STANDARD, domain.getStandardExchangeRate());
        addIfPresent(result, ExchangeRateKind.WIRE_SEND, domain.getWireSendExchangeRate());
        addIfPresent(result, ExchangeRateKind.WIRE_RECEIVE, domain.getWireReceiveExchangeRate());
        addIfPresent(result, ExchangeRateKind.CASH_SELL, domain.getCashSellExchangeRate());
        addIfPresent(result, ExchangeRateKind.CASH_BUY, domain.getCashBuyExchangeRate());
        return result;
    }

    private void addIfPresent(List<ExchangeRateValue> list, ExchangeRateKind kind, BigDecimal rate) {
        if (rate != null) {
            list.add(new ExchangeRateValue(kind, rate));
        }
    }
}
