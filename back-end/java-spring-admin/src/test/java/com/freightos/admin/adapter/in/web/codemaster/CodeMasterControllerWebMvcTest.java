package com.freightos.admin.adapter.in.web.codemaster;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.codemaster.dto.CodeMasterDetailResponse;
import com.freightos.admin.adapter.in.web.codemaster.dto.CodeMasterSummaryResponse;
import com.freightos.admin.adapter.in.web.codemaster.dto.CreateCodeMasterRequest;
import com.freightos.admin.adapter.in.web.codemaster.dto.SearchCodeMasterRequest;
import com.freightos.admin.application.codemaster.port.in.CodeMasterUseCase;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.common.response.PagedResult;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CodeMasterController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class CodeMasterControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CodeMasterUseCase codeMasterUseCase;

    @MockitoBean
    private CodeMasterAssembler codeMasterAssembler;

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
        mockMvc.perform(post("/api/admin/code-master/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── 인증·정상 search → 200 ────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_authenticated_returns200() throws Exception {
        CodeMasterSummaryResponse summaryResponse = new CodeMasterSummaryResponse(
                1L, "USER_STATUS", "사용자 상태", null, 1, true, LocalDateTime.of(2024, 1, 1, 0, 0));
        PagedResult<CodeMasterSummary> summaryPage = PagedResult.of(List.of(), 1L, 1, 0, 20);
        PagedResult<CodeMasterSummaryResponse> responsePage = PagedResult.of(List.of(summaryResponse), 1L, 1, 0, 20);

        given(codeMasterAssembler.toSearchCommand(any())).willReturn(null);
        given(codeMasterUseCase.searchCodeMasters(any())).willReturn(summaryPage);
        given(codeMasterAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchCodeMasterRequest req = new SearchCodeMasterRequest(null, null, null, 0, 20);

        mockMvc.perform(post("/api/admin/code-master/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].masterCode").value("USER_STATUS"));
    }

    // ── 인증·create → 201 + Location + data.id ────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns201WithLocationAndId() throws Exception {
        given(codeMasterAssembler.toCreateCommand(any())).willReturn(null);
        given(codeMasterUseCase.createCodeMaster(any())).willReturn(42L);

        CreateCodeMasterRequest req = new CreateCodeMasterRequest("USER_STATUS", "사용자 상태", null, 1, true);

        mockMvc.perform(post("/api/admin/code-master")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(42));
    }

    // ── 검증 실패 → 400 ────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_blankMasterCode_returns400() throws Exception {
        // masterCode가 빈 문자열 → @NotBlank 위반
        String body = """
                {"masterCode":"","masterName":"사용자 상태","active":true}
                """;
        mockMvc.perform(post("/api/admin/code-master")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── 인증·delete → 200 ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_returns200() throws Exception {
        willDoNothing().given(codeMasterUseCase).deleteCodeMasterById(any());

        mockMvc.perform(delete("/api/admin/code-master/1"))
                .andExpect(status().isOk());
    }

    // ── 상세 조회 getById → 200 ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_returns200WithDetail() throws Exception {
        CodeMasterDetailResponse detail = new CodeMasterDetailResponse(
                1L, "USER_STATUS", "사용자 상태", "사용자의 활성 상태를 나타냅니다.", 1, true,
                LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 2, 0, 0),
                "admin", "admin");

        given(codeMasterUseCase.findCodeMasterById(1L)).willReturn(null);
        given(codeMasterAssembler.toDetail(any())).willReturn(detail);

        mockMvc.perform(get("/api/admin/code-master/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.masterCode").value("USER_STATUS"));
    }
}
