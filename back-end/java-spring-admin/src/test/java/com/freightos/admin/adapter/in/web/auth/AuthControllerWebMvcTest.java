package com.freightos.admin.adapter.in.web.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.auth.port.in.AuthUseCase;
import com.freightos.admin.application.auth.projection.LoginResult;
import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.common.security.JpaUserDetailsService;
import com.freightos.admin.common.security.JwtAuthenticationFilter;
import com.freightos.admin.common.security.JwtTokenProvider;
import com.freightos.admin.common.security.SecurityConfig;
import com.freightos.admin.domain.user.entity.AdminUser;
import com.freightos.admin.domain.user.entity.Permission;
import com.freightos.admin.domain.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserUseCase userUseCase;

    @MockitoBean
    private AuthUseCase authUseCase;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    // ── 미인증 → 401 ──────────────────────────────────────────────────────────

    @Test
    void me_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ── ADMIN role → 200 + role=ADMIN + permissions=[] ───────────────────────

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void me_adminUser_returns200WithRoleAndEmptyPermissions() throws Exception {
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashed", UserRole.ADMIN, true, Set.of());
        user.assignIdentity(1L, null, null, null, null);
        given(userUseCase.findUserByUsername("admin")).willReturn(user);

        mockMvc.perform(get("/api/admin/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.permissions").isArray())
                .andExpect(jsonPath("$.data.permissions").isEmpty());
    }

    // ── USER role + CODE_MANAGE authority → 200 + permissions=["CODE_MANAGE"] ──

    @Test
    @WithMockUser(username = "tester", authorities = {"ROLE_USER", "CODE_MANAGE"})
    void me_userWithCodeManage_returns200WithPermissions() throws Exception {
        AdminUser user = AdminUser.create("tester", "tester@example.com", "hashed", UserRole.USER, true, Set.of(Permission.CODE_MANAGE));
        user.assignIdentity(2L, null, null, null, null);
        given(userUseCase.findUserByUsername("tester")).willReturn(user);

        mockMvc.perform(get("/api/admin/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2L))
                .andExpect(jsonPath("$.data.username").value("tester"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.permissions[0]").value("CODE_MANAGE"));
    }

    // ── login: 정상 → 200 ────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200() throws Exception {
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashed", UserRole.ADMIN, true, Set.of());
        user.assignIdentity(1L, null, null, null, null);
        LoginResult loginResult = new LoginResult("access.token", "refresh.token", user, Set.of());

        given(authUseCase.login(any())).willReturn(loginResult);

        String body = """
                {"username":"admin","password":"pass1234"}
                """;
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh.token"))
                .andExpect(jsonPath("$.data.me.username").value("admin"));
    }

    // ── login: BadCredentials → 401 ──────────────────────────────────────────

    @Test
    void login_badCredentials_returns401() throws Exception {
        given(authUseCase.login(any())).willThrow(new BadCredentialsException("자격 증명이 올바르지 않습니다."));

        String body = """
                {"username":"admin","password":"wrong"}
                """;
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ── refresh: 정상 → 200 ──────────────────────────────────────────────────

    @Test
    void refresh_validToken_returns200() throws Exception {
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashed", UserRole.ADMIN, true, Set.of());
        user.assignIdentity(1L, null, null, null, null);
        LoginResult refreshResult = new LoginResult("new.access.token", "new.refresh.token", user, Set.of());

        given(authUseCase.refresh(any())).willReturn(refreshResult);

        String body = """
                {"refreshToken":"some.refresh.token"}
                """;
        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new.refresh.token"));
    }

    // ── refresh: 유효하지 않은 token → 401 ───────────────────────────────────

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        given(authUseCase.refresh(any())).willThrow(new BadCredentialsException("유효하지 않은 refresh token"));

        String body = """
                {"refreshToken":"invalid.token"}
                """;
        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ── logout: 정상 → 200 ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void logout_authenticated_returns200() throws Exception {
        willDoNothing().given(authUseCase).logout(any());

        String body = """
                {"refreshToken":"some.refresh.token"}
                """;
        mockMvc.perform(post("/api/admin/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
