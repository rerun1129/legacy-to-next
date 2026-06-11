package com.freightos.admin.adapter.in.web.userpermissionpreset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.userpermissionpreset.dto.AssignUserPermissionPresetRequest;
import com.freightos.admin.adapter.in.web.userpermissionpreset.dto.SearchUserPermissionPresetRequest;
import com.freightos.admin.adapter.in.web.userpermissionpreset.dto.UserPermissionPresetResponse;
import com.freightos.admin.application.permissionpreset.projection.UserPermissionPresetRow;
import com.freightos.admin.application.userpermissionpreset.port.in.AssignUserPermissionPresetUseCase;
import com.freightos.admin.application.userpermissionpreset.port.in.ListUserPermissionPresetUseCase;
import com.freightos.admin.application.userpermissionpreset.port.in.RevokeUserPermissionPresetUseCase;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.security.HeaderAuthenticationFilter;
import com.freightos.admin.common.security.JpaUserDetailsService;
import com.freightos.admin.common.security.SecurityConfig;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserPermissionPresetController.class)
@Import({ SecurityConfig.class, HeaderAuthenticationFilter.class })
class UserPermissionPresetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssignUserPermissionPresetUseCase assignUseCase;

    @MockitoBean
    private RevokeUserPermissionPresetUseCase revokeUseCase;

    @MockitoBean
    private ListUserPermissionPresetUseCase listUseCase;

    @MockitoBean
    private UserPermissionPresetWebAssembler assembler;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    // ── 미인증 → 401 ─────────────────────────────────────────────────────────

    @Test
    void search_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/access/user-permission-preset/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    // ── search: MENU authority + userId 기준 목록 반환 ────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_ACCESS_PERMISSION_PRESET")
    void search_withMenuAuthority_returns200AndList() throws Exception {
        UserPermissionPresetRow row = new UserPermissionPresetRow(10L, 1L, 2L, "PRESET_A", "Preset A", true);
        UserPermissionPresetResponse response = new UserPermissionPresetResponse(10L, 1L, 2L, "PRESET_A", "Preset A", true);

        given(listUseCase.listUserPermissionPresets(1L)).willReturn(List.of(row));
        given(assembler.toResponse(row)).willReturn(response);

        SearchUserPermissionPresetRequest req = new SearchUserPermissionPresetRequest(1L);

        mockMvc.perform(post("/api/admin/access/user-permission-preset/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].presetCode").value("PRESET_A"));
    }

    // ── assign: BTN authority → 201 + Location 헤더 ───────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE")
    void assign_withBtnAuthority_returns201WithLocation() throws Exception {
        AssignUserPermissionPresetRequest req = new AssignUserPermissionPresetRequest(1L, 2L);
        UserPermissionPresetRow row = new UserPermissionPresetRow(10L, 1L, 2L, "PRESET_A", "Preset A", true);
        UserPermissionPresetResponse response = new UserPermissionPresetResponse(10L, 1L, 2L, "PRESET_A", "Preset A", true);

        given(assignUseCase.assignUserPermissionPreset(1L, 2L)).willReturn(10L);
        given(listUseCase.listUserPermissionPresets(any())).willReturn(List.of(row));
        given(assembler.toResponse(row)).willReturn(response);

        mockMvc.perform(post("/api/admin/access/user-permission-preset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    // ── assign: userId null → 400 ─────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE")
    void assign_nullUserId_returns400() throws Exception {
        String body = """
                {"userId":null,"presetId":2}
                """;
        mockMvc.perform(post("/api/admin/access/user-permission-preset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── assign: 이미 부여됨 → 409 ─────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE")
    void assign_alreadyAssigned_returns409() throws Exception {
        AssignUserPermissionPresetRequest req = new AssignUserPermissionPresetRequest(1L, 2L);

        given(assignUseCase.assignUserPermissionPreset(1L, 2L)).willThrow(
                ApplicationException.conflict("USER_PERMISSION_PRESET_ALREADY_ASSIGNED",
                        MessageCode.USER_PERMISSION_PRESET_ALREADY_ASSIGNED.getMessage()));

        mockMvc.perform(post("/api/admin/access/user-permission-preset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    // ── revoke: BTN authority → 200 ───────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE")
    void revoke_withBtnAuthority_returns200() throws Exception {
        willDoNothing().given(revokeUseCase).revokeUserPermissionPreset(10L);

        mockMvc.perform(delete("/api/admin/access/user-permission-preset/10"))
                .andExpect(status().isOk());
    }

    // ── revoke: 미존재 id → 404 ───────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE")
    void revoke_notFound_returns404() throws Exception {
        willThrow(ApplicationException.notFound("USER_PERMISSION_PRESET_NOT_FOUND",
                MessageCode.USER_PERMISSION_PRESET_NOT_FOUND.getMessage()))
                .given(revokeUseCase).revokeUserPermissionPreset(99L);

        mockMvc.perform(delete("/api/admin/access/user-permission-preset/99"))
                .andExpect(status().isNotFound());
    }
}
