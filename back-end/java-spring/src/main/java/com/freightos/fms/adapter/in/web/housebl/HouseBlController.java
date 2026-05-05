package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.SearchHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.port.in.HouseBlUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "House B/L", description = "House B/L CRUD — S-02/S-03")
@RestController
@RequestMapping("/api/house-bl")
@RequiredArgsConstructor
@Validated
public class HouseBlController {

    private final HouseBlUseCase houseBlUseCase;
    private final HouseBlAssembler houseBlAssembler;

    @Operation(summary = "House B/L 리스트 조회 (운송 모드 + 방향)")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResult<HouseBlSummaryResponse>>> getHouseBlsByJobDivAndBound(
            @Parameter(description = "운송 모드") @RequestParam JobDiv jobDiv,
            @Parameter(description = "방향")     @RequestParam(required = false) Bound bound,
            @RequestParam(defaultValue = "0")  @Min(0)  int page,
            @RequestParam(defaultValue = "50") @Min(1)  int size,
            @RequestParam(required = false) String hblNo,
            @RequestParam(required = false) String mblNo,
            @RequestParam(required = false) String shipperCode,
            @RequestParam(required = false) String consigneeCode,
            @RequestParam(required = false) String polCode,
            @RequestParam(required = false) String podCode,
            @RequestParam(required = false) String etdFrom,
            @RequestParam(required = false) String etdTo,
            @RequestParam(required = false) String vessel,
            @RequestParam(required = false) String voyage,
            @RequestParam(required = false) String linerCode,
            @RequestParam(required = false) String operatorCode,
            @RequestParam(required = false) String teamCode,
            @RequestParam(required = false) String partyCode,
            @RequestParam(required = false) String portCode) {

        boolean hasFilter = StringUtils.hasText(hblNo) || StringUtils.hasText(mblNo)
                || StringUtils.hasText(shipperCode) || StringUtils.hasText(consigneeCode)
                || StringUtils.hasText(polCode) || StringUtils.hasText(podCode)
                || StringUtils.hasText(etdFrom) || StringUtils.hasText(etdTo)
                || StringUtils.hasText(vessel) || StringUtils.hasText(voyage)
                || StringUtils.hasText(linerCode) || StringUtils.hasText(operatorCode)
                || StringUtils.hasText(teamCode) || StringUtils.hasText(partyCode)
                || StringUtils.hasText(portCode);

        if (hasFilter) {
            HouseBlFilter filter = HouseBlFilter.of(jobDiv, bound, hblNo, mblNo,
                    shipperCode, consigneeCode, polCode, podCode, etdFrom, etdTo,
                    vessel, voyage, linerCode, operatorCode, teamCode, partyCode, portCode);
            return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toSummaryPage(
                    houseBlUseCase.searchHouseBls(filter, PageRequest.of(page, size)))));
        }

        if (bound == null) {
            HouseBlFilter filter = HouseBlFilter.of(jobDiv, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null);
            return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toSummaryPage(
                    houseBlUseCase.searchHouseBls(filter, PageRequest.of(page, size)))));
        }

        return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toSummaryPage(
                houseBlUseCase.getHouseBlsByJobDivAndBound(jobDiv, bound, PageRequest.of(page, size)))));
    }

    @Operation(summary = "House B/L 검색 (POST body)")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<HouseBlSummaryResponse>>> searchHouseBls(
            @Valid @RequestBody SearchHouseBlRequest req) {
        HouseBlFilter filter = HouseBlFilter.of(
                req.jobDiv(), req.bound(),
                req.hblNo(), null,
                null, null,
                null, null,
                req.etdFrom(), req.etdTo(),
                req.vessel(), req.voyage(),
                req.linerCode(), req.operatorCode(),
                req.teamCode(), req.partyCode(), req.portCode());
        return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toSummaryPage(
                houseBlUseCase.searchHouseBls(filter, PageRequest.of(req.page(), req.size())))));
    }

    @Operation(summary = "House B/L 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<HouseBlDetailResponse>> createHouseBl(
            @Valid @RequestBody CreateHouseBlRequest request,
            UriComponentsBuilder uriBuilder) {
        HouseBl saved = houseBlUseCase.save(houseBlAssembler.toEntity(request));
        URI location = uriBuilder.path("/api/house-bl/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(houseBlAssembler.toDetail(saved), MessageCode.HOUSE_BL_CREATED.message()));
    }

    @Operation(summary = "House B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HouseBlDetailResponse>> getHouseBlById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toDetail(houseBlUseCase.findHouseBlById(id))));
    }

    @Operation(summary = "House B/L 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HouseBlDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHouseBlRequest req) {
        HouseBl existing = houseBlUseCase.findHouseBlById(id);
        houseBlAssembler.applyToEntity(req, existing);
        HouseBl saved = houseBlUseCase.save(existing);
        return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toDetail(saved)));
    }

    @Operation(summary = "House B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHouseBlById(@PathVariable Long id) {
        houseBlUseCase.deleteHouseBlById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.HOUSE_BL_DELETED.message()));
    }
}
