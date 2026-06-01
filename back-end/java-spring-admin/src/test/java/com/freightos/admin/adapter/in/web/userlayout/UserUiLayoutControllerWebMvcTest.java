package com.freightos.admin.adapter.in.web.userlayout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.userlayout.port.in.UserUiLayoutUseCase;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserUiLayoutController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class UserUiLayoutControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserUiLayoutUseCase userUiLayoutUseCase;

    @MockitoBean
    private UserUiLayoutAssembler userUiLayoutAssembler;

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
    void getLayout_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/ui-layout/fms.widgetLayouts.v1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void saveLayout_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/admin/ui-layout/fms.widgetLayouts.v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"payload\":{\"cols\":12}}"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET: 레이아웃 존재 → 200 + data 반환 ───────────────────────────────────

    @Test
    @WithMockUser(username = "admin")
    void getLayout_exists_returns200WithJsonNode() throws Exception {
        String payloadJson = "{\"cols\":12}";
        JsonNode jsonNode = objectMapper.readTree(payloadJson);

        given(userUiLayoutUseCase.getLayout(eq("admin"), eq("fms.widgetLayouts.v1")))
                .willReturn(Optional.of(payloadJson));
        given(userUiLayoutAssembler.toJsonNode(payloadJson)).willReturn(jsonNode);

        mockMvc.perform(get("/api/admin/ui-layout/fms.widgetLayouts.v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cols").value(12));
    }

    // ── GET: 레이아웃 없음 → 200 + data=null ──────────────────────────────────

    @Test
    @WithMockUser(username = "admin")
    void getLayout_notExists_returns200WithNullData() throws Exception {
        given(userUiLayoutUseCase.getLayout(eq("admin"), eq("fms.widgetLayouts.v1")))
                .willReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/ui-layout/fms.widgetLayouts.v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // ── PUT: 저장 성공 → 200 ───────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin")
    void saveLayout_valid_returns200() throws Exception {
        String body = "{\"payload\":{\"cols\":12}}";
        given(userUiLayoutAssembler.toPayloadString(any())).willReturn("{\"cols\":12}");
        willDoNothing().given(userUiLayoutUseCase).saveLayout(eq("admin"), eq("fms.widgetLayouts.v1"), any());

        mockMvc.perform(put("/api/admin/ui-layout/fms.widgetLayouts.v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("UI 레이아웃이 저장되었습니다."));
    }

    // ── PUT: payload null → 400 ────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin")
    void saveLayout_nullPayload_returns400() throws Exception {
        mockMvc.perform(put("/api/admin/ui-layout/fms.widgetLayouts.v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"payload\":null}"))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE: 삭제 성공 → 200 ───────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin")
    void deleteLayout_valid_returns200() throws Exception {
        willDoNothing().given(userUiLayoutUseCase).deleteLayout(eq("admin"), eq("fms.widgetLayouts.v1"));

        mockMvc.perform(delete("/api/admin/ui-layout/fms.widgetLayouts.v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("UI 레이아웃이 삭제되었습니다."));
    }
}
