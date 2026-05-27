package com.freightos.admin.adapter.in.web.code.country;

import com.freightos.admin.adapter.in.web.code.country.dto.CountryDetailResponse;
import com.freightos.admin.adapter.in.web.code.country.dto.CountrySummaryResponse;
import com.freightos.admin.adapter.in.web.code.country.dto.CreateCountryRequest;
import com.freightos.admin.adapter.in.web.code.country.dto.SaveCountryChangesRequest;
import com.freightos.admin.adapter.in.web.code.country.dto.SearchCountryRequest;
import com.freightos.admin.adapter.in.web.code.country.dto.UpdateCountryRequest;
import com.freightos.admin.application.code.country.command.CreateCountryCommand;
import com.freightos.admin.application.code.country.command.SaveCountryChangesCommand;
import com.freightos.admin.application.code.country.command.SearchCountryCommand;
import com.freightos.admin.application.code.country.command.UpdateCountryCommand;
import com.freightos.admin.application.code.country.projection.CountrySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.country.entity.Country;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CountryAssembler {

    public SearchCountryCommand toSearchCommand(SearchCountryRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchCountryCommand(req.countryCode(), req.name(), req.scope(), req.page(), size);
    }

    public CreateCountryCommand toCreateCommand(CreateCountryRequest req) {
        return new CreateCountryCommand(req.countryCode(), req.name(), req.nameEn(), req.active());
    }

    public UpdateCountryCommand toUpdateCommand(UpdateCountryRequest req) {
        return new UpdateCountryCommand(req.name(), req.nameEn(), req.active());
    }

    public CountrySummaryResponse toSummaryResponse(CountrySummary p) {
        return new CountrySummaryResponse(p.id(), p.countryCode(), p.name(), p.nameEn(), p.active(), p.deletedAt(), p.updatedAt());
    }

    public CountryDetailResponse toDetail(Country domain) {
        return new CountryDetailResponse(
                domain.getId(), domain.getCountryCode(), domain.getName(), domain.getNameEn(),
                domain.isActive(), domain.getDeletedAt(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<CountrySummaryResponse> toSummaryPage(PagedResult<CountrySummary> src) {
        return src.map(this::toSummaryResponse);
    }

    public SaveCountryChangesCommand toSaveChangesCommand(SaveCountryChangesRequest req) {
        List<CreateCountryCommand> creates = req.creates() == null ? List.of()
                : req.creates().stream().map(this::toCreateCommand).toList();
        List<SaveCountryChangesCommand.UpdateEntry> updates = req.updates() == null ? List.of()
                : req.updates().stream()
                        .map(u -> new SaveCountryChangesCommand.UpdateEntry(u.id(), new UpdateCountryCommand(u.name(), u.nameEn(), u.active())))
                        .toList();
        List<Long> deleteIds = req.deleteIds() == null ? List.of() : req.deleteIds();
        return new SaveCountryChangesCommand(creates, updates, deleteIds);
    }
}
