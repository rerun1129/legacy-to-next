package com.freightos.admin.adapter.in.web.menu;

import com.freightos.admin.adapter.in.web.menu.dto.AccessibleMenuResponse;
import com.freightos.admin.adapter.in.web.menu.dto.CreateMenuRequest;
import com.freightos.admin.adapter.in.web.menu.dto.MenuDetailResponse;
import com.freightos.admin.adapter.in.web.menu.dto.MenuSummaryResponse;
import com.freightos.admin.adapter.in.web.menu.dto.SearchMenuRequest;
import com.freightos.admin.adapter.in.web.menu.dto.UpdateMenuRequest;
import com.freightos.admin.application.menu.port.in.MenuUseCase;
import com.freightos.admin.application.menu.projection.MenuSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/access/menu")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class MenuController {

    private final MenuUseCase menuUseCase;
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

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateMenuRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = menuUseCase.createMenu(menuAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/access/menu/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.MENU_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuRequest req) {
        menuUseCase.updateMenu(id, menuAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.MENU_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        menuUseCase.deleteMenuById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.MENU_DELETED.getMessage()));
    }
}
