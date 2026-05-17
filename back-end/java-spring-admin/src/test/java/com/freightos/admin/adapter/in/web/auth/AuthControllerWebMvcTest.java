package com.freightos.admin.adapter.in.web.auth;

import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.common.security.JpaUserDetailsService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserUseCase userUseCase;

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
}
