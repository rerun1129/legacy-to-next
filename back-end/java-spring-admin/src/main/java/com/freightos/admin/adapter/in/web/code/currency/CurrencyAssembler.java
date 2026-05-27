package com.freightos.admin.adapter.in.web.code.currency;

import com.freightos.admin.adapter.in.web.code.currency.dto.CreateCurrencyRequest;
import com.freightos.admin.adapter.in.web.code.currency.dto.CurrencyDetailResponse;
import com.freightos.admin.adapter.in.web.code.currency.dto.CurrencySummaryResponse;
import com.freightos.admin.adapter.in.web.code.currency.dto.SearchCurrencyRequest;
import com.freightos.admin.adapter.in.web.code.currency.dto.UpdateCurrencyRequest;
import com.freightos.admin.application.code.currency.command.CreateCurrencyCommand;
import com.freightos.admin.application.code.currency.command.SearchCurrencyCommand;
import com.freightos.admin.application.code.currency.command.UpdateCurrencyCommand;
import com.freightos.admin.application.code.currency.projection.CurrencySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.currency.entity.Currency;
import org.springframework.stereotype.Component;

@Component
public class CurrencyAssembler {

    public SearchCurrencyCommand toSearchCommand(SearchCurrencyRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchCurrencyCommand(req.currencyCode(), req.name(), req.scope(), req.page(), size);
    }

    public CreateCurrencyCommand toCreateCommand(CreateCurrencyRequest req) {
        return new CreateCurrencyCommand(req.currencyCode(), req.name(), req.nameEn(), req.symbol(), req.currencyUnit(), req.active());
    }

    public UpdateCurrencyCommand toUpdateCommand(UpdateCurrencyRequest req) {
        return new UpdateCurrencyCommand(req.name(), req.nameEn(), req.symbol(), req.currencyUnit(), req.active());
    }

    public CurrencySummaryResponse toSummaryResponse(CurrencySummary p) {
        return new CurrencySummaryResponse(p.id(), p.currencyCode(), p.name(), p.nameEn(), p.symbol(), p.currencyUnit(), p.active(), p.deletedAt(), p.updatedAt());
    }

    public CurrencyDetailResponse toDetail(Currency domain) {
        return new CurrencyDetailResponse(
                domain.getId(), domain.getCurrencyCode(), domain.getName(), domain.getNameEn(),
                domain.getSymbol(), domain.getCurrencyUnit(), domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<CurrencySummaryResponse> toSummaryPage(PagedResult<CurrencySummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
