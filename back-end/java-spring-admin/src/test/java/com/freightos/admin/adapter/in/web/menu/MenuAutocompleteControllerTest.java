package com.freightos.admin.adapter.in.web.menu;

import com.freightos.admin.application.menu.port.in.MenuUseCase;
import com.freightos.admin.application.menu.port.in.SaveMenuChangesUseCase;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.security.HeaderAuthenticationFilter;
import com.freightos.admin.common.security.JpaUserDetailsService;
import com.freightos.admin.common.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
@Import({ SecurityConfig.class, HeaderAuthenticationFilter.class })
class MenuAutocompleteControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    // ── MENU_ 권한 보유 → 200 + 제안 목록 반환 ─────────────────────────────────

    @Test
    @WithMockUser(authorities = { "MENU_ADMIN_ACCESS_MENU" })
    void autocomplete_withMenuAuthority_returns200WithSuggestions() throws Exception {
        List<AutocompleteItem> items = List.of(
                new AutocompleteItem("ADMIN_CODE_LIST", "코드 관리"),
                new AutocompleteItem("ADMIN_CODE_DETAIL", "코드 상세")
        );
        given(menuUseCase.autocompleteMenuCodes("CODE", 20)).willReturn(items);

        mockMvc.perform(get("/api/admin/access/menu/autocomplete")
                        .param("query", "CODE")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("ADMIN_CODE_LIST"))
                .andExpect(jsonPath("$.data[0].name").value("코드 관리"));
    }

    // ── 미인증 → 401 ──────────────────────────────────────────────────────────

    @Test
    void autocomplete_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/access/menu/autocomplete")
                        .param("query", "CODE"))
                .andExpect(status().isUnauthorized());
    }
}
