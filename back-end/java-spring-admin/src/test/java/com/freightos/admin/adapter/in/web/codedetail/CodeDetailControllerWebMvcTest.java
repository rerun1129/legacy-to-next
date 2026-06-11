package com.freightos.admin.adapter.in.web.codedetail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.codedetail.dto.CodeDetailDetailResponse;
import com.freightos.admin.adapter.in.web.codedetail.dto.CodeDetailSummaryResponse;
import com.freightos.admin.adapter.in.web.codedetail.dto.CreateCodeDetailRequest;
import com.freightos.admin.adapter.in.web.codedetail.dto.SearchCodeDetailRequest;
import com.freightos.admin.application.codedetail.port.in.CodeDetailUseCase;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
import com.freightos.admin.common.response.PagedResult;
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

@WebMvcTest(CodeDetailController.class)
@Import({ SecurityConfig.class, HeaderAuthenticationFilter.class })
class CodeDetailControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CodeDetailUseCase codeDetailUseCase;

    @MockitoBean
    private CodeDetailAssembler codeDetailAssembler;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    // ── 미인증 → 401 ─────────────────────────────────────────────────────────

    @Test
    void search_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/code-detail/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── MENU_ADMIN_CODE_LIST authority → 200 ──────────────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CODE_LIST")
    void search_authenticated_returns200() throws Exception {
        CodeDetailSummaryResponse summaryResponse = new CodeDetailSummaryResponse(
                10L, 1L, "ACTIVE", "활성", 1, true, null, LocalDateTime.of(2024, 1, 1, 0, 0));
        PagedResult<CodeDetailSummary> summaryPage = PagedResult.of(List.of(), 1L, 1, 0, 20);
        PagedResult<CodeDetailSummaryResponse> responsePage = PagedResult.of(List.of(summaryResponse), 1L, 1, 0, 20);

        given(codeDetailAssembler.toSearchCommand(any())).willReturn(null);
        given(codeDetailUseCase.searchCodeDetails(any())).willReturn(summaryPage);
        given(codeDetailAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchCodeDetailRequest req = new SearchCodeDetailRequest(1L, null, null, null, 0, 20);

        mockMvc.perform(post("/api/admin/code-detail/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(10L))
                .andExpect(jsonPath("$.data.content[0].codeValue").value("ACTIVE"));
    }

    // ── BTN_ADMIN_CODE_LIST_DETAIL_SAVE authority → 201 ──────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CODE_LIST_DETAIL_SAVE")
    void create_returns201WithLocationAndId() throws Exception {
        given(codeDetailAssembler.toCreateCommand(any())).willReturn(null);
        given(codeDetailUseCase.createCodeDetail(any())).willReturn(55L);

        CreateCodeDetailRequest req = new CreateCodeDetailRequest(1L, "ACTIVE", "활성", 1, true, null);

        mockMvc.perform(post("/api/admin/code-detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(55));
    }

    // ── 검증 실패 → 400 ────────────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CODE_LIST_DETAIL_SAVE")
    void create_blankCodeValue_returns400() throws Exception {
        // codeValue가 빈 문자열 → @NotBlank 위반
        String body = """
                {"masterId":1,"codeValue":"","codeLabel":"활성","active":true}
                """;
        mockMvc.perform(post("/api/admin/code-detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── BTN_ADMIN_CODE_LIST_DETAIL_SAVE authority → 200 ──────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CODE_LIST_DETAIL_SAVE")
    void delete_returns200() throws Exception {
        willDoNothing().given(codeDetailUseCase).deleteCodeDetailById(any());

        mockMvc.perform(delete("/api/admin/code-detail/1"))
                .andExpect(status().isOk());
    }

    // ── MENU_ADMIN_CODE_LIST authority → getById 200 ─────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CODE_LIST")
    void getById_returns200WithDetail() throws Exception {
        CodeDetailDetailResponse detail = new CodeDetailDetailResponse(
                10L, 1L, "ACTIVE", "활성", 1, true, "활성 상태",
                LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 2, 0, 0),
                "admin", "admin");

        given(codeDetailUseCase.findCodeDetailById(10L)).willReturn(null);
        given(codeDetailAssembler.toDetail(any())).willReturn(detail);

        mockMvc.perform(get("/api/admin/code-detail/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.codeValue").value("ACTIVE"));
    }
}
