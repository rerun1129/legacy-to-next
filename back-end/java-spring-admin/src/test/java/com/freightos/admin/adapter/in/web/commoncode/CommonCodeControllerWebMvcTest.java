package com.freightos.admin.adapter.in.web.commoncode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.commoncode.dto.SaveCommonCodeChangesRequest;
import com.freightos.admin.application.commoncode.port.in.CommonCodeUseCase;
import com.freightos.admin.application.commoncode.projection.CommonCodeGroupSummary;
import com.freightos.admin.application.commoncode.projection.CommonCodeSummary;
import com.freightos.admin.common.response.SaveChangesResult;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommonCodeController.class)
@Import({ SecurityConfig.class, HeaderAuthenticationFilter.class })
class CommonCodeControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommonCodeUseCase commonCodeUseCase;

    @MockitoBean
    private CommonCodeAssembler commonCodeAssembler;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    // ── 미인증 → 401 ──────────────────────────────────────────────────────────

    @Test
    void getGroups_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/common-code/groups"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void saveChanges_unauthenticated_returns401() throws Exception {
        SaveCommonCodeChangesRequest req = new SaveCommonCodeChangesRequest("Bound", List.of(), List.of());
        mockMvc.perform(post("/api/admin/common-code/save-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── MENU 권한 있음: 조회 200 ──────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = {"MENU_ADMIN_COMMON_CODE"})
    void getGroups_withMenuAuth_returns200() throws Exception {
        CommonCodeGroupSummary summary = new CommonCodeGroupSummary(1L, "Bound", "FMS", null, true);
        given(commonCodeUseCase.getCommonCodeGroups()).willReturn(List.of(summary));
        given(commonCodeAssembler.toGroupResponseList(any())).willReturn(List.of());

        mockMvc.perform(get("/api/admin/common-code/groups"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"MENU_ADMIN_COMMON_CODE"})
    void getCodes_withMenuAuth_returns200() throws Exception {
        CommonCodeSummary codeSummary = new CommonCodeSummary(1L, "Bound", "EXP", "Export", "수출", 0, true);
        given(commonCodeUseCase.getCommonCodesByGroup("Bound")).willReturn(List.of(codeSummary));
        given(commonCodeAssembler.toCodeResponseList(any())).willReturn(List.of());

        mockMvc.perform(get("/api/admin/common-code").param("group", "Bound"))
                .andExpect(status().isOk());
    }

    // ── BTN 권한 없이 save-changes → 403 ─────────────────────────────────────

    @Test
    @WithMockUser(authorities = {"MENU_ADMIN_COMMON_CODE"})
    void saveChanges_withoutBtnAuth_returns403() throws Exception {
        SaveCommonCodeChangesRequest req = new SaveCommonCodeChangesRequest("Bound", List.of(), List.of());
        mockMvc.perform(post("/api/admin/common-code/save-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ── BTN + MENU 권한 있음: save-changes 200 ───────────────────────────────

    @Test
    @WithMockUser(authorities = {"MENU_ADMIN_COMMON_CODE", "BTN_COMMON_CODE_SAVE"})
    void saveChanges_withBtnAuth_returns200() throws Exception {
        SaveCommonCodeChangesRequest req = new SaveCommonCodeChangesRequest("Bound", List.of(), List.of());
        given(commonCodeUseCase.saveCommonCodeChanges(any())).willReturn(new SaveChangesResult(0, 0, 0));

        mockMvc.perform(post("/api/admin/common-code/save-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.createdCount").value(0));
    }

    // ── groupCode blank → 400 ─────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = {"MENU_ADMIN_COMMON_CODE", "BTN_COMMON_CODE_SAVE"})
    void saveChanges_blankGroupCode_returns400() throws Exception {
        SaveCommonCodeChangesRequest req = new SaveCommonCodeChangesRequest("", List.of(), List.of());
        mockMvc.perform(post("/api/admin/common-code/save-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
