package com.freightos.admin.adapter.in.web.partner;

import com.freightos.admin.adapter.in.web.partner.dto.CreatePartnerRequest;
import com.freightos.admin.adapter.in.web.partner.dto.PartnerDetailResponse;
import com.freightos.admin.adapter.in.web.partner.dto.PartnerSummaryResponse;
import com.freightos.admin.adapter.in.web.partner.dto.SearchPartnerRequest;
import com.freightos.admin.adapter.in.web.partner.dto.UpdatePartnerRequest;
import com.freightos.admin.application.partner.port.in.PartnerUseCase;
import com.freightos.admin.application.partner.projection.PartnerSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.partner.entity.Partner;
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
@RequestMapping("/api/admin/partner")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN') or hasAuthority('PARTNER_MANAGE')")
public class PartnerController {

    private final PartnerUseCase partnerUseCase;
    private final PartnerAssembler partnerAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<PartnerSummaryResponse>>> search(
            @Valid @RequestBody SearchPartnerRequest req) {
        PagedResult<PartnerSummary> result = partnerUseCase.searchPartners(partnerAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(partnerAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerDetailResponse>> getById(@PathVariable Long id) {
        Partner domain = partnerUseCase.getPartnerById(id);
        return ResponseEntity.ok(ApiResponse.of(partnerAssembler.toDetail(domain)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreatePartnerRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = partnerUseCase.createPartner(partnerAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/partner/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.PARTNER_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePartnerRequest req) {
        partnerUseCase.updatePartner(id, partnerAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.PARTNER_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        partnerUseCase.deletePartner(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.PARTNER_DELETED.getMessage()));
    }
}
