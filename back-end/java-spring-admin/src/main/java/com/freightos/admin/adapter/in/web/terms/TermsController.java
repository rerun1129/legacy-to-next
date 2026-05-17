package com.freightos.admin.adapter.in.web.terms;

import com.freightos.admin.adapter.in.web.terms.dto.CreateTermsRequest;
import com.freightos.admin.adapter.in.web.terms.dto.SearchTermsRequest;
import com.freightos.admin.adapter.in.web.terms.dto.TermsDetailResponse;
import com.freightos.admin.adapter.in.web.terms.dto.TermsSummaryResponse;
import com.freightos.admin.adapter.in.web.terms.dto.UpdateTermsRequest;
import com.freightos.admin.application.terms.port.in.TermsUseCase;
import com.freightos.admin.application.terms.projection.TermsSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.terms.entity.Terms;
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
@RequestMapping("/api/admin/cms/terms")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('CMS_MANAGE') or hasRole('ADMIN')")
public class TermsController {

    private final TermsUseCase termsUseCase;
    private final TermsAssembler termsAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<TermsSummaryResponse>>> search(
            @Valid @RequestBody SearchTermsRequest req) {
        PagedResult<TermsSummary> result = termsUseCase.searchTerms(termsAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(termsAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TermsDetailResponse>> getById(@PathVariable Long id) {
        Terms domain = termsUseCase.getTermsById(id);
        return ResponseEntity.ok(ApiResponse.of(termsAssembler.toDetail(domain)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateTermsRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = termsUseCase.createTerms(termsAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/cms/terms/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.TERMS_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTermsRequest req) {
        termsUseCase.updateTerms(id, termsAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.TERMS_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        termsUseCase.deleteTerms(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.TERMS_DELETED.getMessage()));
    }
}
