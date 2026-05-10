package com.freightos.fms.adapter.in.web.nonbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.ChangeNonBlHblNoRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.CreateNonBlRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.NonBlDetailResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.UpdateNonBlRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.NonBlSummaryResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.SearchNonBlRequest;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.nonbl.port.in.NonBlUseCase;
import com.freightos.fms.common.response.MessageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

@Tag(name = "Non B/L", description = "Non B/L 관리")
@RestController
@RequestMapping("/api/non-bl")
@RequiredArgsConstructor
@Validated
public class NonBlController {

    private final NonBlUseCase nonBlUseCase;
    private final NonBlAssembler nonBlAssembler;

    @Operation(summary = "Non B/L 검색 (POST body)")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<NonBlSummaryResponse>>> searchNonBls(
            @Valid @RequestBody SearchNonBlRequest req) {
        return ResponseEntity.ok(ApiResponse.of(nonBlAssembler.toSummaryPage(
                nonBlUseCase.searchNonBls(
                        nonBlAssembler.toSearchCommand(req),
                        PageRequest.of(req.page(), req.size())))));
    }

    @Operation(summary = "Non B/L 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<NonBlDetailResponse>> createNonBl(
            @Valid @RequestBody CreateNonBlRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = nonBlUseCase.createNonBl(nonBlAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/non-bl/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(
                        nonBlAssembler.toDetail(nonBlUseCase.findNonBlById(id)),
                        MessageCode.NON_BL_CREATED.message()));
    }

    @Operation(summary = "Non B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NonBlDetailResponse>> getNonBlById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(nonBlAssembler.toDetail(nonBlUseCase.findNonBlById(id))));
    }

    @Operation(summary = "Non B/L 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NonBlDetailResponse>> updateNonBl(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNonBlRequest req) {
        return ResponseEntity.ok(ApiResponse.of(
                nonBlAssembler.toDetail(nonBlUseCase.updateNonBl(id, nonBlAssembler.toUpdateCommand(req)))));
    }

    @Operation(summary = "Non B/L 번호 변경 (전용 엔드포인트)")
    @PutMapping("/{id}/hbl-no")
    public ResponseEntity<ApiResponse<Void>> changeHblNo(
            @PathVariable Long id,
            @Valid @RequestBody ChangeNonBlHblNoRequest req) {
        nonBlUseCase.changeNonBlHblNo(id, new ChangeHouseBlNoCommand(req.hblNo()));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.NON_BL_UPDATED.message()));
    }

    @Operation(summary = "Non B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNonBlById(@PathVariable Long id) {
        nonBlUseCase.deleteNonBlById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.NON_BL_DELETED.message()));
    }
}
