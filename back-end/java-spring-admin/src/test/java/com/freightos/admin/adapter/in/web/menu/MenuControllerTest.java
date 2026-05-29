package com.freightos.admin.adapter.in.web.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.menu.dto.AccessibleMenuResponse;
import com.freightos.admin.application.menu.port.in.MenuUseCase;
import com.freightos.admin.application.menu.port.in.SaveMenuChangesUseCase;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.common.security.JpaUserDetailsService;
import com.freightos.admin.common.security.JwtAuthenticationFilter;
import com.freightos.admin.common.security.JwtTokenProvider;
import com.freightos.admin.common.security.SecurityConfig;
import com.freightos.admin.domain.menu.entity.Menu;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuUseCase menuUseCase;

    @MockitoBean
    private SaveMenuChangesUseCase saveMenuChangesUseCase;

    @MockitoBean
    private MenuAssembler menuAssembler;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    // ── MENU_ 권한 보유 → 200, useCase 호출 Set 검증 ─────────────────────────

    @Test
    @WithMockUser(authorities = { "MENU_ADMIN_CODE_LIST", "MENU_ADMIN_USER_LIST" })
    void accessible_withAdminAuthorities_returnsFilteredMenus() throws Exception {
        Menu child = Menu.create("ADMIN_CODE_LIST", 10L, "/code", "코드관리", null, null, 1, true, "ACCESS");
        child.assignIdentity(20L, null, null, null, null);

        Menu parent = Menu.create("ADMIN_PARENT", null, "/parent", "부모", null, null, 1, true, "ACCESS");
        parent.assignIdentity(10L, null, null, null, null);

        List<Menu> domainList = List.of(child, parent);
        List<AccessibleMenuResponse> responseList = List.of(
                new AccessibleMenuResponse(20L, "ADMIN_CODE_LIST", 10L, "/code", "코드관리", null, null, 1, "ACCESS"),
                new AccessibleMenuResponse(10L, "ADMIN_PARENT", null, "/parent", "부모", null, null, 1, "ACCESS")
        );

        ArgumentCaptor<Set<String>> codesCaptor = ArgumentCaptor.captor();
        given(menuUseCase.findAccessibleAdminMenus(any())).willReturn(domainList);
        given(menuAssembler.toAccessibleList(domainList)).willReturn(responseList);

        mockMvc.perform(get("/api/admin/access/menu/accessible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].menuCode").value("ADMIN_CODE_LIST"));

        then(menuUseCase).should().findAccessibleAdminMenus(codesCaptor.capture());
        assertThat(codesCaptor.getValue()).containsExactlyInAnyOrder("ADMIN_CODE_LIST", "ADMIN_USER_LIST");
    }

    // ── MENU_ prefix 없는 권한 → useCase 호출 빈 Set, 응답 빈 배열 ──────────

    @Test
    @WithMockUser(authorities = { "ROLE_USER" })
    void accessible_withEmptyMenuAuthorities_returnsEmptyList() throws Exception {
        given(menuUseCase.findAccessibleAdminMenus(Set.of())).willReturn(List.of());
        given(menuAssembler.toAccessibleList(List.of())).willReturn(List.of());

        mockMvc.perform(get("/api/admin/access/menu/accessible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ── 미인증 → 401 ──────────────────────────────────────────────────────────

    @Test
    void accessible_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/access/menu/accessible"))
                .andExpect(status().isUnauthorized());
    }

    // ── save-changes: MENU + BTN_SAVE 권한 → 200 + 카운트 검증 ────────────────

    @Test
    @WithMockUser(authorities = { "MENU_ADMIN_ACCESS_MENU", "BTN_ADMIN_ACCESS_MENU_SAVE" })
    void saveChanges_withMenuAndBtnAuthority_returns200WithCounts() throws Exception {
        SaveChangesResult result = new SaveChangesResult(1, 0, 0);
        String body = """
                {
                  "creates": [
                    {
                      "menuCode": "ADMIN_TEST_MENU",
                      "parentId": null,
                      "path": "/test",
                      "label": "테스트",
                      "labelEn": null,
                      "icon": null,
                      "sortOrder": 0,
                      "active": true,
                      "moduleCode": "ADMIN"
                    }
                  ],
                  "updates": []
                }
                """;

        given(menuAssembler.toSaveChangesCommand(any())).willReturn(null);
        given(saveMenuChangesUseCase.saveMenuChanges(any())).willReturn(result);

        mockMvc.perform(post("/api/admin/access/menu/save-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.createdCount").value(1))
                .andExpect(jsonPath("$.data.updatedCount").value(0))
                .andExpect(jsonPath("$.data.deletedCount").value(0));

        then(saveMenuChangesUseCase).should().saveMenuChanges(any());
    }

    // ── save-changes: MENU 권한만(BTN_SAVE 없음) → 403 ───────────────────────

    @Test
    @WithMockUser(authorities = { "MENU_ADMIN_ACCESS_MENU" })
    void saveChanges_withoutBtnSaveAuthority_returns403() throws Exception {
        String body = """
                {"creates": [], "updates": []}
                """;

        mockMvc.perform(post("/api/admin/access/menu/save-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // ── save-changes: 미인증 → 401 ────────────────────────────────────────────

    @Test
    void saveChanges_withoutAuthentication_returns401() throws Exception {
        String body = """
                {"creates": [], "updates": []}
                """;

        mockMvc.perform(post("/api/admin/access/menu/save-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
