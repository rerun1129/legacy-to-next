package com.freightos.admin.adapter.in.web.code.currency;

import com.freightos.admin.adapter.in.web.code.currency.dto.CreateCurrencyRequest;
import com.freightos.admin.adapter.in.web.code.currency.dto.CurrencyDetailResponse;
import com.freightos.admin.adapter.in.web.code.currency.dto.CurrencySummaryResponse;
import com.freightos.admin.adapter.in.web.code.currency.dto.SaveCurrencyChangesRequest;
import com.freightos.admin.adapter.in.web.code.currency.dto.SearchCurrencyRequest;
import com.freightos.admin.adapter.in.web.code.currency.dto.UpdateCurrencyRequest;
import com.freightos.admin.application.code.currency.port.in.CurrencyUseCase;
import com.freightos.admin.application.code.currency.projection.CurrencySummary;
import com.freightos.admin.common.request.BulkDeleteRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.currency.entity.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/code/currency")
@RequiredArgsConstructor
@Validated
public class CurrencyController {

    private final CurrencyUseCase currencyUseCase;
    private final CurrencyAssembler currencyAssembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_CURRENCY')")
    public ResponseEntity<ApiResponse<PagedResult<CurrencySummaryResponse>>> search(
            @Valid @RequestBody SearchCurrencyRequest req) {
        PagedResult<CurrencySummary> result = currencyUseCase.searchCurrencies(currencyAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(currencyAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_CURRENCY')")
    public ResponseEntity<ApiResponse<CurrencyDetailResponse>> getById(@PathVariable Long id) {
        Currency domain = currencyUseCase.getCurrencyById(id);
        return ResponseEntity.ok(ApiResponse.of(currencyAssembler.toDetail(domain)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_CURRENCY_CREATE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateCurrencyRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = currencyUseCase.createCurrency(currencyAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/code/currency/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.CURRENCY_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_CURRENCY_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCurrencyRequest req) {
        currencyUseCase.updateCurrency(id, currencyAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CURRENCY_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_CURRENCY_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        currencyUseCase.deleteCurrency(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CURRENCY_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_CURRENCY_DELETE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest req) {
        currencyUseCase.deleteCurrencies(req.ids());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CURRENCY_DELETED.getMessage()));
    }

    @PostMapping("/save-changes")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_CURRENCY_CREATE')")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @Valid @RequestBody SaveCurrencyChangesRequest req) {
        SaveChangesResult result = currencyUseCase.saveCurrencyChanges(currencyAssembler.toSaveChangesCommand(req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.CURRENCY_SAVE_CHANGES.getMessage()));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_CURRENCY')")
    public ResponseEntity<ApiResponse<List<AutocompleteItem>>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(ApiResponse.of(currencyUseCase.autocompleteCurrencies(q, limit)));
    }
}
