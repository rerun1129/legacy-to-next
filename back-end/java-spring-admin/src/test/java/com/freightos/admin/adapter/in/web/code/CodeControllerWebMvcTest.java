package com.freightos.admin.adapter.in.web.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.code.dto.CodeDetailResponse;
import com.freightos.admin.adapter.in.web.code.dto.CodeSummaryResponse;
import com.freightos.admin.adapter.in.web.code.dto.CreateCodeRequest;
import com.freightos.admin.adapter.in.web.code.dto.SearchCodeRequest;
import com.freightos.admin.application.code.port.in.CodeUseCase;
import com.freightos.admin.application.code.projection.CodeSummary;
import com.freightos.admin.common.response.PagedResult;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CodeController.class)
@Import(SecurityConfig.class)
class CodeControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CodeUseCase codeUseCase;

    @MockitoBean
    private CodeAssembler codeAssembler;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    // ── 미인증 → 401 ─────────────────────────────────────────────────────────

    @Test
    void search_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/code/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── 인증·정상 search → 200 ────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_authenticated_returns200() throws Exception {
        CodeSummaryResponse summaryResponse = new CodeSummaryResponse(
                1L, "CARRIER", "KR001", "Korean Carrier", 1, true, LocalDateTime.of(2024, 1, 1, 0, 0));
        PagedResult<CodeSummary> summaryPage = PagedResult.of(List.of(), 1L, 1, 0, 20);
        PagedResult<CodeSummaryResponse> responsePage = PagedResult.of(List.of(summaryResponse), 1L, 1, 0, 20);

        given(codeAssembler.toSearchCommand(any())).willReturn(null);
        given(codeUseCase.searchCodes(any())).willReturn(summaryPage);
        given(codeAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchCodeRequest req = new SearchCodeRequest(null, null, null, null, 0, 20);

        mockMvc.perform(post("/api/admin/code/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].codeGroup").value("CARRIER"));
    }

    // ── 인증·create → 201 + Location + data.id ────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns201WithLocationAndId() throws Exception {
        given(codeAssembler.toCreateCommand(any())).willReturn(null);
        given(codeUseCase.createCode(any())).willReturn(42L);

        CreateCodeRequest req = new CreateCodeRequest("CARRIER", "KR001", "Korean Carrier", 1, true, null);

        mockMvc.perform(post("/api/admin/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(42));
    }

    // ── 검증 실패 → 400 ────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_blankCodeGroup_returns400() throws Exception {
        // codeGroup이 빈 문자열 → @NotBlank 위반
        String body = """
                {"codeGroup":"","codeValue":"KR001","codeLabel":"Korean Carrier","active":true}
                """;
        mockMvc.perform(post("/api/admin/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── 인증·delete → 200 ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_returns200() throws Exception {
        willDoNothing().given(codeUseCase).deleteCodeById(any());

        mockMvc.perform(delete("/api/admin/code/1"))
                .andExpect(status().isOk());
    }

    // ── 상세 조회용 보조 fixture (CodeDetailResponse 사용 확인) ─────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_returns200WithDetail() throws Exception {
        CodeDetailResponse detail = new CodeDetailResponse(
                1L, "CARRIER", "KR001", "Korean Carrier", 1, true,
                "국내 운송사 코드", LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 2, 0, 0), "admin", "admin");

        given(codeUseCase.findCodeById(1L)).willReturn(null);
        given(codeAssembler.toDetail(any())).willReturn(detail);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/admin/code/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.remark").value("국내 운송사 코드"));
    }
}
