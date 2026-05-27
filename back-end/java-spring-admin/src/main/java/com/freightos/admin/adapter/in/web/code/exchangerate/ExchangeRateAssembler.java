package com.freightos.admin.adapter.in.web.code.exchangerate;

import com.freightos.admin.adapter.in.web.code.exchangerate.dto.CreateExchangeRateRequest;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.ExchangeRateDetailResponse;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.ExchangeRateSummaryResponse;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.SaveExchangeRateChangesRequest;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.SearchExchangeRateRequest;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.UpdateExchangeRateRequest;
import com.freightos.admin.application.code.exchangerate.command.CreateExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.command.SaveExchangeRateChangesCommand;
import com.freightos.admin.application.code.exchangerate.command.SearchExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.command.UpdateExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExchangeRateAssembler {

    public SearchExchangeRateCommand toSearchCommand(SearchExchangeRateRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchExchangeRateCommand(req.fromCurrencyCode(), req.toCurrencyCode(), req.name(), req.scope(), req.page(), size);
    }

    public CreateExchangeRateCommand toCreateCommand(CreateExchangeRateRequest req) {
        return new CreateExchangeRateCommand(
                req.fromCurrencyCode(), req.toCurrencyCode(), req.exchangeDate(),
                req.cashSellExchangeRate(), req.cashBuyExchangeRate(),
                req.wireSendExchangeRate(), req.wireReceiveExchangeRate(),
                req.standardExchangeRate(), req.name(), req.nameEn(), req.active()
        );
    }

    public UpdateExchangeRateCommand toUpdateCommand(UpdateExchangeRateRequest req) {
        return new UpdateExchangeRateCommand(
                req.cashSellExchangeRate(), req.cashBuyExchangeRate(),
                req.wireSendExchangeRate(), req.wireReceiveExchangeRate(),
                req.standardExchangeRate(), req.name(), req.nameEn(), req.active()
        );
    }

    public ExchangeRateSummaryResponse toSummaryResponse(ExchangeRateSummary p) {
        return new ExchangeRateSummaryResponse(p.id(), p.fromCurrencyCode(), p.toCurrencyCode(), p.exchangeDate(), p.cashSellExchangeRate(), p.cashBuyExchangeRate(), p.wireSendExchangeRate(), p.wireReceiveExchangeRate(), p.standardExchangeRate(), p.name(), p.nameEn(), p.active(), p.deletedAt(), p.updatedAt());
    }

    public ExchangeRateDetailResponse toDetail(ExchangeRate domain) {
        return new ExchangeRateDetailResponse(
                domain.getId(), domain.getFromCurrencyCode(), domain.getToCurrencyCode(), domain.getExchangeDate(),
                domain.getCashSellExchangeRate(), domain.getCashBuyExchangeRate(),
                domain.getWireSendExchangeRate(), domain.getWireReceiveExchangeRate(),
                domain.getStandardExchangeRate(), domain.getName(), domain.getNameEn(),
                domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<ExchangeRateSummaryResponse> toSummaryPage(PagedResult<ExchangeRateSummary> src) {
        return src.map(this::toSummaryResponse);
    }

    public SaveExchangeRateChangesCommand toSaveChangesCommand(SaveExchangeRateChangesRequest req) {
        List<CreateExchangeRateCommand> creates = req.creates() == null ? List.of()
                : req.creates().stream().map(this::toCreateCommand).toList();
        List<SaveExchangeRateChangesCommand.UpdateEntry> updates = req.updates() == null ? List.of()
                : req.updates().stream()
                        .map(u -> new SaveExchangeRateChangesCommand.UpdateEntry(u.id(),
                                new UpdateExchangeRateCommand(u.cashSellExchangeRate(), u.cashBuyExchangeRate(),
                                        u.wireSendExchangeRate(), u.wireReceiveExchangeRate(),
                                        u.standardExchangeRate(), u.name(), u.nameEn(), u.active())))
                        .toList();
        List<Long> deleteIds = req.deleteIds() == null ? List.of() : req.deleteIds();
        return new SaveExchangeRateChangesCommand(creates, updates, deleteIds);
    }
}
