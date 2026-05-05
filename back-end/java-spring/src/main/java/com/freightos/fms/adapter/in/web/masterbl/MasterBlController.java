package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.CreateMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.UpdateMasterBlRequest;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.masterbl.port.in.MasterBlUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Tag(name = "Master B/L", description = "Master B/L CRUD — S-04/S-05")
@RestController
@RequestMapping("/api/master-bl")
@RequiredArgsConstructor
@Validated
public class MasterBlController {

    private final MasterBlUseCase masterBlUseCase;
    private final MasterBlAssembler masterBlAssembler;

    @Operation(summary = "Master B/L 리스트 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResult<MasterBlSummaryResponse>>> getMasterBlsByBound(
            @RequestParam Bound bound,
            @RequestParam(defaultValue = "0")  @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) int size,
            @RequestParam(required = false) String mblNo,
            @RequestParam(required = false) String shipperCode,
            @RequestParam(required = false) String consigneeCode,
            @RequestParam(required = false) String polCode,
            @RequestParam(required = false) String podCode,
            @RequestParam(required = false) String etdFrom,
            @RequestParam(required = false) String etdTo) {

        boolean hasFilter = StringUtils.hasText(mblNo) || StringUtils.hasText(shipperCode)
                || StringUtils.hasText(consigneeCode) || StringUtils.hasText(polCode)
                || StringUtils.hasText(podCode) || StringUtils.hasText(etdFrom)
                || StringUtils.hasText(etdTo);

        if (hasFilter) {
            MasterBlFilter filter = new MasterBlFilter(bound, mblNo, shipperCode, consigneeCode,
                    polCode, podCode, etdFrom, etdTo);
            return ResponseEntity.ok(ApiResponse.of(
                    masterBlAssembler.toSummaryPage(masterBlUseCase.searchMasterBls(filter, PageRequest.of(page, size)))));
        }

        return ResponseEntity.ok(ApiResponse.of(
                masterBlAssembler.toSummaryPage(masterBlUseCase.getMasterBlsByBound(bound, PageRequest.of(page, size)))));
    }

    @Operation(summary = "Master B/L 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<MasterBlDetailResponse>> createMasterBl(
            @Valid @RequestBody CreateMasterBlRequest request,
            UriComponentsBuilder uriBuilder) {
        MasterBl saved = masterBlUseCase.save(masterBlAssembler.toEntity(request));
        URI location = uriBuilder.path("/api/master-bl/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(masterBlAssembler.toDetail(masterBlUseCase.findMasterBlDetailById(saved.getId())),
                        MessageCode.MASTER_BL_CREATED.message()));
    }

    @Operation(summary = "Master B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasterBlDetailResponse>> findMasterBlById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(masterBlAssembler.toDetail(masterBlUseCase.findMasterBlDetailById(id))));
    }

    @Operation(summary = "Master B/L 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MasterBlDetailResponse>> updateMasterBl(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMasterBlRequest req) {
        MasterBl entity = masterBlUseCase.findMasterBlById(id);
        masterBlAssembler.applyToEntity(req, entity);
        masterBlUseCase.save(entity);
        return ResponseEntity.ok(ApiResponse.of(
                masterBlAssembler.toDetail(masterBlUseCase.findMasterBlDetailById(id)),
                MessageCode.MASTER_BL_UPDATED.message()));
    }

    @Operation(summary = "Master B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMasterBlById(@PathVariable Long id) {
        masterBlUseCase.deleteMasterBlById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.MASTER_BL_DELETED.message()));
    }
}
