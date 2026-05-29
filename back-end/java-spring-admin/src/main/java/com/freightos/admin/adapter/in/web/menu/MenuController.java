package com.freightos.admin.adapter.in.web.menu;

import com.freightos.admin.adapter.in.web.menu.dto.AccessibleMenuResponse;
import com.freightos.admin.adapter.in.web.menu.dto.MenuDetailResponse;
import com.freightos.admin.adapter.in.web.menu.dto.MenuSummaryResponse;
import com.freightos.admin.adapter.in.web.menu.dto.SaveMenuChangesRequest;
import com.freightos.admin.adapter.in.web.menu.dto.SearchMenuRequest;
import com.freightos.admin.application.menu.port.in.MenuUseCase;
import com.freightos.admin.application.menu.port.in.SaveMenuChangesUseCase;
import com.freightos.admin.application.menu.projection.MenuSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/access/menu")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('MENU_ADMIN_ACCESS_MENU')")
public class MenuController {

    private final MenuUseCase menuUseCase;
    private final SaveMenuChangesUseCase saveMenuChangesUseCase;
    private final MenuAssembler menuAssembler;

    @GetMapping("/accessible")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AccessibleMenuResponse>>> getAccessibleMenus(Authentication auth) {
        Set<String> menuCodes = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("MENU_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toSet());
        List<AccessibleMenuResponse> data = menuAssembler.toAccessibleList(menuUseCase.findAccessibleAdminMenus(menuCodes));
        return ResponseEntity.ok(ApiResponse.of(data));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<MenuSummaryResponse>>> search(
            @Valid @RequestBody SearchMenuRequest req) {
        PagedResult<MenuSummary> result = menuUseCase.searchMenus(menuAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(menuAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(menuAssembler.toDetail(menuUseCase.findMenuById(id))));
    }

    @PostMapping("/save-changes")
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_MENU_SAVE')")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @Valid @RequestBody SaveMenuChangesRequest req) {
        SaveChangesResult result = saveMenuChangesUseCase.saveMenuChanges(menuAssembler.toSaveChangesCommand(req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.MENU_SAVE_CHANGES.getMessage()));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<AutocompleteItem>>> autocomplete(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(ApiResponse.of(menuUseCase.autocompleteMenuCodes(query, limit)));
    }
}
