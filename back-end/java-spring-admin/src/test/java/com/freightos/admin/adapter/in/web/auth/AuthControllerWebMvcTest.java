package com.freightos.admin.adapter.in.web.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.auth.port.in.AuthUseCase;
import com.freightos.admin.application.auth.projection.LoginResult;
import com.freightos.admin.application.auth.projection.MeProjection;
import com.freightos.admin.common.security.HeaderAuthenticationFilter;
import com.freightos.admin.common.security.JpaUserDetailsService;
import com.freightos.admin.common.security.SecurityConfig;
import com.freightos.admin.domain.user.entity.AdminUser;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, HeaderAuthenticationFilter.class })
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthUseCase authUseCase;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    // ── 미인증 → 401 ──────────────────────────────────────────────────────────

    @Test
    void me_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ── ADMIN role → 200 + attributes 포함, role 필드 없음 ────────────────────

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void me_adminUser_returns200WithAttributesAndEmptyMenus() throws Exception {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        MeProjection projection = new MeProjection(1L, "admin", "admin@example.com",
                attrs, List.of(), List.of());
        given(authUseCase.getMe("admin")).willReturn(projection);

        mockMvc.perform(get("/api/admin/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("admin"))
                // role 필드는 더 이상 존재하지 않음 (Phase 4: attributes로 이전)
                .andExpect(jsonPath("$.data.role").doesNotExist())
                .andExpect(jsonPath("$.data.accessibleMenus").isArray())
                .andExpect(jsonPath("$.data.accessibleMenus").isEmpty());
    }

    // ── USER role → 200 + accessibleMenus 포함 ──────────────────────────────

    @Test
    @WithMockUser(username = "tester", authorities = {"ROLE_USER", "MENU_ADMIN_CODE_LIST"})
    void me_userWithMenuAuthority_returns200WithMenus() throws Exception {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        MeProjection projection = new MeProjection(2L, "tester", "tester@example.com",
                attrs, List.of("MENU_ADMIN_CODE_LIST"), List.of());
        given(authUseCase.getMe("tester")).willReturn(projection);

        mockMvc.perform(get("/api/admin/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2L))
                .andExpect(jsonPath("$.data.username").value("tester"))
                .andExpect(jsonPath("$.data.accessibleMenus[0]").value("MENU_ADMIN_CODE_LIST"));
    }

    // ── login: 정상 → 200 ────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200() throws Exception {
        Map<String, List<String>> attrsAdmin = Map.of("role", List.of("ADMIN"));
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashed", true, attrsAdmin, null, null);
        user.assignIdentity(1L, null, null, null, null);
        LoginResult loginResult = new LoginResult("access.token", "refresh.token", user,
                attrsAdmin, List.of(), List.of());

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
        Map<String, List<String>> attrsAdmin = Map.of("role", List.of("ADMIN"));
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashed", true, attrsAdmin, null, null);
        user.assignIdentity(1L, null, null, null, null);
        LoginResult refreshResult = new LoginResult("new.access.token", "new.refresh.token", user,
                attrsAdmin, List.of(), List.of());

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
