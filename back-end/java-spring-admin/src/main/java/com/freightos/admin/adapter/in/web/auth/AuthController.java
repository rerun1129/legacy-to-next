package com.freightos.admin.adapter.in.web.auth;

import com.freightos.admin.adapter.in.web.auth.dto.MeResponse;
import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.domain.user.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserUseCase userUseCase;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AdminUser user = userUseCase.findUserByUsername(auth.getName());
        MeResponse response = new MeResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getPermissions().stream().map(Enum::name).sorted().toList()
        );
        return ResponseEntity.ok(ApiResponse.of(response, MessageCode.AUTH_ME_OK.getMessage()));
    }
}
