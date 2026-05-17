package com.freightos.admin.adapter.in.web.terms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.terms.dto.CreateTermsRequest;
import com.freightos.admin.adapter.in.web.terms.dto.SearchTermsRequest;
import com.freightos.admin.adapter.in.web.terms.dto.TermsSummaryResponse;
import com.freightos.admin.application.terms.command.CreateTermsCommand;
import com.freightos.admin.application.terms.command.SearchTermsCommand;
import com.freightos.admin.application.terms.port.in.TermsUseCase;
import com.freightos.admin.application.terms.projection.TermsSummary;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TermsController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class TermsControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TermsUseCase termsUseCase;

    @MockitoBean
    private TermsAssembler termsAssembler;

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
    void search_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/cms/terms/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── ADMIN search → 200 ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_adminAuthenticated_returns200() throws Exception {
        TermsSummaryResponse summaryResponse = new TermsSummaryResponse(1L, "TOS", 1, null, null, null, null);
        PagedResult<TermsSummary> summaryPage = PagedResult.of(List.of(), 1L, 1, 0, 20);
        PagedResult<TermsSummaryResponse> responsePage = PagedResult.of(List.of(summaryResponse), 1L, 1, 0, 20);

        given(termsAssembler.toSearchCommand(any())).willReturn(new SearchTermsCommand(null, "ACTIVE", null, null, 0, 20));
        given(termsUseCase.searchTerms(any())).willReturn(summaryPage);
        given(termsAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchTermsRequest req = new SearchTermsRequest(null, "ACTIVE", null, null, 0, 20);

        mockMvc.perform(post("/api/admin/cms/terms/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].termsId").value(1L))
                .andExpect(jsonPath("$.data.content[0].type").value("TOS"));
    }

    // ── ADMIN create → 201 + Location + data.id ──────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns201WithLocationAndId() throws Exception {
        given(termsAssembler.toCreateCommand(any())).willReturn(
                new CreateTermsCommand("TOS", 1, LocalDateTime.of(2024, 1, 1, 0, 0), "약관 본문", null));
        given(termsUseCase.createTerms(any())).willReturn(42L);

        CreateTermsRequest req = new CreateTermsRequest("TOS", 1, LocalDateTime.of(2024, 1, 1, 0, 0), "약관 본문", null);

        mockMvc.perform(post("/api/admin/cms/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(42));
    }

    // ── content @NotBlank → 400 ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_blankContent_returns400() throws Exception {
        String body = """
                {"type":"TOS","version":1,"effectiveAt":"2024-01-01T00:00:00","content":""}
                """;
        mockMvc.perform(post("/api/admin/cms/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── type @NotBlank → 400 ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_blankType_returns400() throws Exception {
        String body = """
                {"type":"","version":1,"effectiveAt":"2024-01-01T00:00:00","content":"약관 본문"}
                """;
        mockMvc.perform(post("/api/admin/cms/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── version @Min(1) 위반 → 400 ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_versionLessThan1_returns400() throws Exception {
        String body = """
                {"type":"TOS","version":0,"effectiveAt":"2024-01-01T00:00:00","content":"약관 본문"}
                """;
        mockMvc.perform(post("/api/admin/cms/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── CMS_MANAGE authority → 200 ───────────────────────────────────────────

    @Test
    @WithMockUser(authorities = {"ROLE_USER", "CMS_MANAGE"})
    void search_withCmsManageAuthority_returns200() throws Exception {
        PagedResult<TermsSummary> summaryPage = PagedResult.of(List.of(), 0L, 0, 0, 20);
        PagedResult<TermsSummaryResponse> responsePage = PagedResult.of(List.of(), 0L, 0, 0, 20);

        given(termsAssembler.toSearchCommand(any())).willReturn(new SearchTermsCommand(null, null, null, null, 0, 20));
        given(termsUseCase.searchTerms(any())).willReturn(summaryPage);
        given(termsAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchTermsRequest req = new SearchTermsRequest(null, null, null, null, 0, 20);

        mockMvc.perform(post("/api/admin/cms/terms/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── CODE_MANAGE only (CMS_MANAGE 없음) → 403 ─────────────────────────────

    @Test
    @WithMockUser(authorities = {"ROLE_USER", "CODE_MANAGE"})
    void search_withCodeManageOnly_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/cms/terms/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isForbidden());
    }

    // ── delete ADMIN → 200 ────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_adminAuthenticated_returns200() throws Exception {
        willDoNothing().given(termsUseCase).deleteTerms(any());

        mockMvc.perform(delete("/api/admin/cms/terms/1"))
                .andExpect(status().isOk());
    }
}
