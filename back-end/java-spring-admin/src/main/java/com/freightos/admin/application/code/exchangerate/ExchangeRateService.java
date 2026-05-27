package com.freightos.admin.application.code.exchangerate;

import com.freightos.admin.application.code.exchangerate.command.CreateExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.command.SearchExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.command.UpdateExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.port.in.ExchangeRateUseCase;
import com.freightos.admin.application.code.exchangerate.port.out.ExchangeRatePort;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
