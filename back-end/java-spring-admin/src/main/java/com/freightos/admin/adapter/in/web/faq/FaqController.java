package com.freightos.admin.adapter.in.web.faq;

import com.freightos.admin.adapter.in.web.faq.dto.CreateFaqRequest;
import com.freightos.admin.adapter.in.web.faq.dto.FaqDetailResponse;
import com.freightos.admin.adapter.in.web.faq.dto.FaqSummaryResponse;
import com.freightos.admin.adapter.in.web.faq.dto.SearchFaqRequest;
import com.freightos.admin.adapter.in.web.faq.dto.UpdateFaqRequest;
import com.freightos.admin.application.faq.port.in.FaqUseCase;
import com.freightos.admin.application.faq.projection.FaqSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faq.entity.Faq;
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
@RequestMapping("/api/admin/cms/faq")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('CMS_MANAGE') or hasRole('ADMIN')")
public class FaqController {

    private final FaqUseCase faqUseCase;
    private final FaqAssembler faqAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<FaqSummaryResponse>>> search(
            @Valid @RequestBody SearchFaqRequest req) {
        PagedResult<FaqSummary> result = faqUseCase.searchFaqs(faqAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(faqAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FaqDetailResponse>> getById(@PathVariable Long id) {
        Faq domain = faqUseCase.getFaqById(id);
        return ResponseEntity.ok(ApiResponse.of(faqAssembler.toDetail(domain)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateFaqRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = faqUseCase.createFaq(faqAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/cms/faq/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.FAQ_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFaqRequest req) {
        faqUseCase.updateFaq(id, faqAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.FAQ_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        faqUseCase.deleteFaq(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.FAQ_DELETED.getMessage()));
    }
}
