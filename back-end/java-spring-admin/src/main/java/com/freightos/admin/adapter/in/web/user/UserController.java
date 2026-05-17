package com.freightos.admin.adapter.in.web.user;

import com.freightos.admin.adapter.in.web.user.dto.CreateUserRequest;
import com.freightos.admin.adapter.in.web.user.dto.SearchUserRequest;
import com.freightos.admin.adapter.in.web.user.dto.UpdateUserRequest;
import com.freightos.admin.adapter.in.web.user.dto.UserDetailResponse;
import com.freightos.admin.adapter.in.web.user.dto.UserSummaryResponse;
import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.user.entity.AdminUser;
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
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_MANAGE')")
public class UserController {

    private final UserUseCase userUseCase;
    private final UserAssembler userAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<UserSummaryResponse>>> search(
            @Valid @RequestBody SearchUserRequest req) {
        PagedResult<UserSummary> result = userUseCase.searchUsers(userAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(userAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getById(@PathVariable Long id) {
        AdminUser domain = userUseCase.findUserById(id);
        return ResponseEntity.ok(ApiResponse.of(userAssembler.toDetail(domain)));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateUserRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = userUseCase.createUser(userAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/user/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.USER_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest req) {
        userUseCase.updateUser(id, userAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.USER_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        userUseCase.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.USER_DELETED.getMessage()));
    }
}
