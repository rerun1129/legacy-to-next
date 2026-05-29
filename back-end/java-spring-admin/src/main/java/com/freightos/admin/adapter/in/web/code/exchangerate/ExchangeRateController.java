package com.freightos.admin.adapter.in.web.code.exchangerate;

import com.freightos.admin.adapter.in.web.code.exchangerate.dto.CreateExchangeRateRequest;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.ExchangeRateDetailResponse;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.ExchangeRateSummaryResponse;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.SaveExchangeRateChangesRequest;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.SearchExchangeRateRequest;
import com.freightos.admin.adapter.in.web.code.exchangerate.dto.UpdateExchangeRateRequest;
import com.freightos.admin.application.code.exchangerate.port.in.ExchangeRateUseCase;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
import com.freightos.admin.common.request.BulkDeleteRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRate;
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
@RequestMapping("/api/admin/code/exchange-rate")
@RequiredArgsConstructor
@Validated
public class ExchangeRateController {

    private final ExchangeRateUseCase exchangeRateUseCase;
    private final ExchangeRateAssembler exchangeRateAssembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_EXCHANGE_RATE')")
    public ResponseEntity<ApiResponse<PagedResult<ExchangeRateSummaryResponse>>> search(
            @Valid @RequestBody SearchExchangeRateRequest req) {
        PagedResult<ExchangeRateSummary> result = exchangeRateUseCase.searchExchangeRates(exchangeRateAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(exchangeRateAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_EXCHANGE_RATE')")
    public ResponseEntity<ApiResponse<ExchangeRateDetailResponse>> getById(@PathVariable Long id) {
        ExchangeRate domain = exchangeRateUseCase.getExchangeRateById(id);
        return ResponseEntity.ok(ApiResponse.of(exchangeRateAssembler.toDetail(domain)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_EXCHANGE_RATE_SAVE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateExchangeRateRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = exchangeRateUseCase.createExchangeRate(exchangeRateAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/code/exchange-rate/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.EXCHANGE_RATE_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_EXCHANGE_RATE_SAVE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExchangeRateRequest req) {
        exchangeRateUseCase.updateExchangeRate(id, exchangeRateAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.EXCHANGE_RATE_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_EXCHANGE_RATE_SAVE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        exchangeRateUseCase.deleteExchangeRate(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.EXCHANGE_RATE_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_EXCHANGE_RATE_SAVE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest req) {
        exchangeRateUseCase.deleteExchangeRates(req.ids());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.EXCHANGE_RATE_DELETED.getMessage()));
    }

    @PostMapping("/save-changes")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_EXCHANGE_RATE_SAVE')")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @Valid @RequestBody SaveExchangeRateChangesRequest req) {
        SaveChangesResult result = exchangeRateUseCase.saveExchangeRateChanges(exchangeRateAssembler.toSaveChangesCommand(req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.EXCHANGE_RATE_SAVE_CHANGES.getMessage()));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_EXCHANGE_RATE')")
    public ResponseEntity<ApiResponse<List<AutocompleteItem>>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(ApiResponse.of(exchangeRateUseCase.autocompleteExchangeRates(q, limit)));
    }
}
