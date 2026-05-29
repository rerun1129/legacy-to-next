package com.freightos.admin.adapter.in.web.permissionpreset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.AssignAttributeValuesRequest;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.CreatePermissionPresetRequest;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.PermissionPresetResponse;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.PermissionPresetSummaryResponse;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.SearchPermissionPresetRequest;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.UpdatePermissionPresetRequest;
import com.freightos.admin.application.permissionpreset.command.AssignAttributeValuesCommand;
import com.freightos.admin.application.permissionpreset.command.CreatePermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.command.ListPermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.command.UpdatePermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.port.in.AssignAttributeValuesToPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.AutocompletePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.CreatePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.DeletePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.GetPermissionPresetDetailUseCase;
import com.freightos.admin.application.permissionpreset.port.in.ListPermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.SavePermissionPresetChangesUseCase;
import com.freightos.admin.application.permissionpreset.port.in.UpdatePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.projection.PermissionPresetDetail;
import com.freightos.admin.application.permissionpreset.projection.PermissionPresetSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.security.JpaUserDetailsService;
import com.freightos.admin.common.security.JwtAuthenticationFilter;
import com.freightos.admin.common.security.JwtTokenProvider;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PermissionPresetController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class PermissionPresetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreatePermissionPresetUseCase createUseCase;

    @MockitoBean
    private UpdatePermissionPresetUseCase updateUseCase;

    @MockitoBean
    private DeletePermissionPresetUseCase deleteUseCase;

    @MockitoBean
    private GetPermissionPresetDetailUseCase getDetailUseCase;

    @MockitoBean
    private ListPermissionPresetUseCase listUseCase;

    @MockitoBean
    private AssignAttributeValuesToPresetUseCase assignUseCase;

    @MockitoBean
    private SavePermissionPresetChangesUseCase saveChangesUseCase;

    @MockitoBean
    private AutocompletePermissionPresetUseCase autocompleteUseCase;

    @MockitoBean
    private PermissionPresetWebAssembler assembler;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    // ── 미인증 → 401 ─────────────────────────────────────────────────────────

    @Test
    void search_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/access/permission-preset/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── search: MENU authority → 200 + 목록 반환 ──────────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_ACCESS_PERMISSION_PRESET")
    void search_withMenuAuthority_returns200AndList() throws Exception {
        PermissionPresetSummary summary = new PermissionPresetSummary(1L, "PRESET_ADMIN", "Admin", "desc", true, List.of(10L));
        PermissionPresetSummaryResponse response = new PermissionPresetSummaryResponse(1L, "PRESET_ADMIN", "Admin", "desc", true, List.of(10L));

        given(assembler.toListCommand(any())).willReturn(new ListPermissionPresetCommand(false));
        given(listUseCase.listPermissionPresets(any())).willReturn(List.of(summary));
        given(assembler.toSummaryResponse(summary)).willReturn(response);

        mockMvc.perform(post("/api/admin/access/permission-preset/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("PRESET_ADMIN"))
                .andExpect(jsonPath("$.data[0].active").value(true));
    }

    // ── getById: MENU authority → 200 + 상세 반환 ─────────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_ACCESS_PERMISSION_PRESET")
    void getById_withMenuAuthority_returns200() throws Exception {
        PermissionPresetDetail detail = new PermissionPresetDetail(1L, "PRESET_ADMIN", "Admin", "desc", true, List.of(10L), List.of());
        PermissionPresetResponse response = new PermissionPresetResponse(1L, "PRESET_ADMIN", "Admin", "desc", true, List.of(10L), List.of());

        given(getDetailUseCase.getPermissionPresetDetail(1L)).willReturn(detail);
        given(assembler.toDetailResponse(detail)).willReturn(response);

        mockMvc.perform(get("/api/admin/access/permission-preset/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("PRESET_ADMIN"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    // ── create: BTN authority → 201 + Location 헤더 + id 반환 ─────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE")
    void create_withBtnAuthority_returns201WithLocationAndId() throws Exception {
        CreatePermissionPresetRequest req = new CreatePermissionPresetRequest("PRESET_TEST", "Test", null, true);

        given(assembler.toCreateCommand(any())).willReturn(new CreatePermissionPresetCommand("PRESET_TEST", "Test", null, true));
        given(createUseCase.createPermissionPreset(any())).willReturn(42L);

        mockMvc.perform(post("/api/admin/access/permission-preset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(42));
    }

    // ── create: code @NotBlank 미충족 → 400 ──────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE")
    void create_blankCode_returns400() throws Exception {
        String body = """
                {"code":"","name":"Test","active":true}
                """;
        mockMvc.perform(post("/api/admin/access/permission-preset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── update: BTN authority → 200 ───────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE")
    void update_withBtnAuthority_returns200() throws Exception {
        UpdatePermissionPresetRequest req = new UpdatePermissionPresetRequest("Updated", null, true);

        given(assembler.toUpdateCommand(any())).willReturn(new UpdatePermissionPresetCommand("Updated", null, true));
        willDoNothing().given(updateUseCase).updatePermissionPreset(eq(1L), any());

        mockMvc.perform(put("/api/admin/access/permission-preset/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── delete: BTN authority → 200 ───────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE")
    void delete_withBtnAuthority_returns200() throws Exception {
        willDoNothing().given(deleteUseCase).deletePermissionPreset(1L);

        mockMvc.perform(delete("/api/admin/access/permission-preset/1"))
                .andExpect(status().isOk());
    }

    // ── delete: RESTRICT(부여된 user 존재) → 409 ──────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE")
    void delete_presetInUse_returns409() throws Exception {
        willThrow(ApplicationException.conflict(
                "PERMISSION_PRESET_IN_USE_CANNOT_DELETE",
                MessageCode.PERMISSION_PRESET_IN_USE_CANNOT_DELETE.getMessage()))
                .given(deleteUseCase).deletePermissionPreset(1L);

        mockMvc.perform(delete("/api/admin/access/permission-preset/1"))
                .andExpect(status().isConflict());
    }

    // ── assignAttributeValues: BTN authority → 200 ────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE")
    void assignAttributeValues_withBtnAuthority_returns200() throws Exception {
        AssignAttributeValuesRequest req = new AssignAttributeValuesRequest(List.of(10L), List.of(5L));

        given(assembler.toAssignCommand(any())).willReturn(new AssignAttributeValuesCommand(List.of(10L), List.of(5L)));
        willDoNothing().given(assignUseCase).assignAttributeValuesToPreset(eq(1L), any());

        mockMvc.perform(post("/api/admin/access/permission-preset/1/attribute-values")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── search: 다른 authority → 403 ─────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CODE_LIST")
    void search_withWrongAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/access/permission-preset/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
