package com.freightos.admin.adapter.in.web.code.exchangerate;

import com.freightos.admin.adapter.in.web.code.exchangerate.dto.CreateExchangeRateRequest;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.ExchangeRateDetailResponse;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.ExchangeRateSummaryResponse;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.SearchExchangeRateRequest;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.UpdateExchangeRateRequest;
import com.freightos.admin.application.code.exchangerate.command.CreateExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.command.SearchExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.command.UpdateExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRate;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateAssembler {

    public SearchExchangeRateCommand toSearchCommand(SearchExchangeRateRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchExchangeRateCommand(req.baseCurrency(), req.targetCurrency(), req.name(), req.scope(), req.page(), size);
    }

    public CreateExchangeRateCommand toCreateCommand(CreateExchangeRateRequest req) {
        return new CreateExchangeRateCommand(req.baseCurrency(), req.targetCurrency(), req.rate(), req.name(), req.nameEn(), req.active());
    }

    public UpdateExchangeRateCommand toUpdateCommand(UpdateExchangeRateRequest req) {
        return new UpdateExchangeRateCommand(req.rate(), req.name(), req.nameEn(), req.active());
    }

    public ExchangeRateSummaryResponse toSummaryResponse(ExchangeRateSummary p) {
        return new ExchangeRateSummaryResponse(p.id(), p.baseCurrency(), p.targetCurrency(), p.rate(), p.name(), p.active(), p.deletedAt(), p.updatedAt());
    }

    public ExchangeRateDetailResponse toDetail(ExchangeRate domain) {
        return new ExchangeRateDetailResponse(
                domain.getId(), domain.getBaseCurrency(), domain.getTargetCurrency(), domain.getRate(),
                domain.getName(), domain.getNameEn(), domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<ExchangeRateSummaryResponse> toSummaryPage(PagedResult<ExchangeRateSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
