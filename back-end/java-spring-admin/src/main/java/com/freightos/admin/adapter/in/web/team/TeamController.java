package com.freightos.admin.adapter.in.web.team;

import com.freightos.admin.adapter.in.web.team.dto.TeamAutocompleteResponse;
import com.freightos.admin.adapter.in.web.team.dto.TeamSummaryResponse;
import com.freightos.admin.application.team.port.in.TeamUseCase;
import com.freightos.admin.application.team.projection.TeamAutocompleteItem;
import com.freightos.admin.application.team.projection.TeamSummary;
import com.freightos.admin.common.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Validated
public class TeamController {

    private final TeamUseCase teamUseCase;
    private final TeamAssembler teamAssembler;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TeamSummaryResponse>>> getAllTeams() {
        List<TeamSummary> summaries = teamUseCase.getAllTeams();
        return ResponseEntity.ok(ApiResponse.of(teamAssembler.toSummaryResponseList(summaries)));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TeamAutocompleteResponse>>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        List<TeamAutocompleteItem> items = teamUseCase.autocompleteTeams(q, limit);
        return ResponseEntity.ok(ApiResponse.of(teamAssembler.toAutocompleteResponseList(items)));
    }
}
