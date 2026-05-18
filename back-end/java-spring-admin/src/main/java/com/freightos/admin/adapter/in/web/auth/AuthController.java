package com.freightos.admin.adapter.in.web.auth;

import com.freightos.admin.adapter.in.web.auth.dto.LoginRequest;
import com.freightos.admin.adapter.in.web.auth.dto.LoginResponse;
import com.freightos.admin.adapter.in.web.auth.dto.LogoutRequest;
import com.freightos.admin.adapter.in.web.auth.dto.MeResponse;
import com.freightos.admin.adapter.in.web.auth.dto.RefreshRequest;
import com.freightos.admin.adapter.in.web.auth.dto.RefreshResponse;
import com.freightos.admin.application.auth.command.LoginCommand;
import com.freightos.admin.application.auth.command.LogoutCommand;
import com.freightos.admin.application.auth.command.RefreshCommand;
import com.freightos.admin.application.auth.port.in.AuthUseCase;
import com.freightos.admin.application.auth.projection.LoginResult;
import com.freightos.admin.application.auth.projection.MeProjection;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MeProjection projection = authUseCase.getMe(auth.getName());
        MeResponse response = new MeResponse(
                projection.id(),
                projection.username(),
                projection.email(),
                projection.attributes(),
                projection.accessibleMenus(),
                projection.accessibleButtons()
        );
        return ResponseEntity.ok(ApiResponse.of(response, MessageCode.AUTH_ME_OK.getMessage()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        LoginResult result = authUseCase.login(new LoginCommand(req.username(), req.password()));
        MeResponse me = new MeResponse(
                result.user().getId(),
                result.user().getUsername(),
                result.user().getEmail(),
                result.attributes(),
                result.accessibleMenus(),
                result.accessibleButtons()
        );
        return ResponseEntity.ok(ApiResponse.of(
                new LoginResponse(result.accessToken(), result.refreshToken(), me),
                MessageCode.AUTH_LOGIN_OK.getMessage()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest req) {
        LoginResult result = authUseCase.refresh(new RefreshCommand(req.refreshToken()));
        return ResponseEntity.ok(ApiResponse.of(
                new RefreshResponse(result.accessToken(), result.refreshToken()),
                MessageCode.AUTH_REFRESH_OK.getMessage()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest req) {
        authUseCase.logout(new LogoutCommand(req.refreshToken()));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.AUTH_LOGOUT_OK.getMessage()));
    }
}
