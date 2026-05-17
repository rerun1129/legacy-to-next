package com.freightos.admin.adapter.in.web.faqcategory;

import com.freightos.admin.adapter.in.web.faqcategory.dto.CreateFaqCategoryRequest;
import com.freightos.admin.adapter.in.web.faqcategory.dto.FaqCategoryDetailResponse;
import com.freightos.admin.adapter.in.web.faqcategory.dto.FaqCategorySummaryResponse;
import com.freightos.admin.adapter.in.web.faqcategory.dto.SearchFaqCategoryRequest;
import com.freightos.admin.adapter.in.web.faqcategory.dto.UpdateFaqCategoryRequest;
import com.freightos.admin.application.faqcategory.port.in.FaqCategoryUseCase;
import com.freightos.admin.application.faqcategory.projection.FaqCategorySummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faqcategory.entity.FaqCategory;
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
@RequestMapping("/api/admin/cms/faq-category")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('CMS_MANAGE') or hasRole('ADMIN')")
public class FaqCategoryController {

    private final FaqCategoryUseCase faqCategoryUseCase;
    private final FaqCategoryAssembler faqCategoryAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<FaqCategorySummaryResponse>>> search(
            @Valid @RequestBody SearchFaqCategoryRequest req) {
        PagedResult<FaqCategorySummary> result = faqCategoryUseCase.searchFaqCategories(faqCategoryAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(faqCategoryAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FaqCategoryDetailResponse>> getById(@PathVariable Long id) {
        FaqCategory domain = faqCategoryUseCase.getFaqCategoryById(id);
        return ResponseEntity.ok(ApiResponse.of(faqCategoryAssembler.toDetail(domain)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateFaqCategoryRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = faqCategoryUseCase.createFaqCategory(faqCategoryAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/cms/faq-category/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.FAQ_CATEGORY_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFaqCategoryRequest req) {
        faqCategoryUseCase.updateFaqCategory(id, faqCategoryAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.FAQ_CATEGORY_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        faqCategoryUseCase.deleteFaqCategory(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.FAQ_CATEGORY_DELETED.getMessage()));
    }
}
