package com.freightos.admin.adapter.in.web.code.country;

import com.freightos.admin.adapter.in.web.code.country.dto.CountryDetailResponse;
import com.freightos.admin.adapter.in.web.code.country.dto.CountrySummaryResponse;
import com.freightos.admin.adapter.in.web.code.country.dto.CreateCountryRequest;
import com.freightos.admin.adapter.in.web.code.country.dto.SearchCountryRequest;
import com.freightos.admin.adapter.in.web.code.country.dto.UpdateCountryRequest;
import com.freightos.admin.application.code.country.port.in.CountryUseCase;
import com.freightos.admin.application.code.country.projection.CountrySummary;
import com.freightos.admin.common.request.BulkDeleteRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.country.entity.Country;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/code/country")
@RequiredArgsConstructor
@Validated
public class CountryController {

    private final CountryUseCase countryUseCase;
    private final CountryAssembler countryAssembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_COUNTRY')")
    public ResponseEntity<ApiResponse<PagedResult<CountrySummaryResponse>>> search(
            @Valid @RequestBody SearchCountryRequest req) {
        PagedResult<CountrySummary> result = countryUseCase.searchCountries(countryAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(countryAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_COUNTRY')")
    public ResponseEntity<ApiResponse<CountryDetailResponse>> getById(@PathVariable Long id) {
        Country domain = countryUseCase.getCountryById(id);
        return ResponseEntity.ok(ApiResponse.of(countryAssembler.toDetail(domain)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_COUNTRY_CREATE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateCountryRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = countryUseCase.createCountry(countryAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/code/country/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.COUNTRY_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_COUNTRY_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCountryRequest req) {
        countryUseCase.updateCountry(id, countryAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.COUNTRY_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_COUNTRY_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        countryUseCase.deleteCountry(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.COUNTRY_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_COUNTRY_DELETE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest req) {
        countryUseCase.deleteCountries(req.ids());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.COUNTRY_DELETED.getMessage()));
    }
}
