package com.freightos.admin.adapter.in.web.partner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.partner.dto.CreatePartnerRequest;
import com.freightos.admin.adapter.in.web.partner.dto.PartnerDetailResponse;
import com.freightos.admin.adapter.in.web.partner.dto.PartnerSummaryResponse;
import com.freightos.admin.adapter.in.web.partner.dto.SearchPartnerRequest;
import com.freightos.admin.application.partner.command.CreatePartnerCommand;
import com.freightos.admin.application.partner.command.SearchPartnerCommand;
import com.freightos.admin.application.partner.port.in.PartnerUseCase;
import com.freightos.admin.application.partner.projection.PartnerSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.security.JpaUserDetailsService;
import com.freightos.admin.common.security.JwtAuthenticationFilter;
import com.freightos.admin.common.security.JwtTokenProvider;
import com.freightos.admin.common.security.SecurityConfig;
import com.freightos.admin.domain.partner.entity.PartnerType;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PartnerController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class PartnerControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PartnerUseCase partnerUseCase;

    @MockitoBean
    private PartnerAssembler partnerAssembler;

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
        mockMvc.perform(post("/api/admin/partner/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── 인증·정상 search → 200 ────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_authenticated_returns200() throws Exception {
        PartnerSummaryResponse summaryResponse = new PartnerSummaryResponse(
                1L, "FWD-001", PartnerType.FORWARDER, "글로벌 포워더", true,
                null, LocalDateTime.of(2024, 1, 1, 0, 0));
        PagedResult<PartnerSummary> summaryPage = PagedResult.of(List.of(), 1L, 1, 0, 20);
        PagedResult<PartnerSummaryResponse> responsePage = PagedResult.of(List.of(summaryResponse), 1L, 1, 0, 20);

        given(partnerAssembler.toSearchCommand(any())).willReturn(new SearchPartnerCommand(null, null, null, null, false, 0, 20));
        given(partnerUseCase.searchPartners(any())).willReturn(summaryPage);
        given(partnerAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchPartnerRequest req = new SearchPartnerRequest(null, null, null, null, false, 0, 20);

        mockMvc.perform(post("/api/admin/partner/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].partnerCode").value("FWD-001"));
    }

    // ── 인증·create → 201 + Location + data.id ────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns201WithLocationAndId() throws Exception {
        given(partnerAssembler.toCreateCommand(any())).willReturn(
                new CreatePartnerCommand("FWD-001", PartnerType.FORWARDER, "테스트 포워더",
                        null, null, null, null, null, null, null, true));
        given(partnerUseCase.createPartner(any())).willReturn(42L);

        CreatePartnerRequest req = new CreatePartnerRequest(
                "FWD-001", PartnerType.FORWARDER, "테스트 포워더",
                null, null, null, null, null, null, null, Boolean.TRUE);

        mockMvc.perform(post("/api/admin/partner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(42));
    }

    // ── partnerCode @NotBlank → 400 ──────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_blankPartnerCode_returns400() throws Exception {
        String body = """
                {"partnerCode":"","partnerType":"FORWARDER","name":"테스트","active":true}
                """;
        mockMvc.perform(post("/api/admin/partner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── name @NotBlank → 400 ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_blankName_returns400() throws Exception {
        String body = """
                {"partnerCode":"FWD-001","partnerType":"FORWARDER","name":"","active":true}
                """;
        mockMvc.perform(post("/api/admin/partner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── delete → 200 ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_returns200() throws Exception {
        willDoNothing().given(partnerUseCase).deletePartner(any());

        mockMvc.perform(delete("/api/admin/partner/1"))
                .andExpect(status().isOk());
    }

    // ── getById → 200 + detail ────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_returns200WithDetail() throws Exception {
        PartnerDetailResponse detail = new PartnerDetailResponse(
                1L, "FWD-001", PartnerType.FORWARDER, "글로벌 포워더", "Global Forwarder",
                null, null, null, null, null, null, true, null,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 2, 0, 0),
                "admin", "admin");

        given(partnerUseCase.getPartnerById(1L)).willReturn(null);
        given(partnerAssembler.toDetail(any())).willReturn(detail);

        mockMvc.perform(get("/api/admin/partner/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.partnerCode").value("FWD-001"))
                .andExpect(jsonPath("$.data.partnerType").value("FORWARDER"));
    }

    // ── @PreAuthorize: PARTNER_MANAGE authority → 200 ────────────────────────

    @Test
    @WithMockUser(authorities = {"ROLE_USER", "PARTNER_MANAGE"})
    void search_withPartnerManageAuthority_returns200() throws Exception {
        PagedResult<PartnerSummary> summaryPage = PagedResult.of(List.of(), 0L, 0, 0, 20);
        PagedResult<PartnerSummaryResponse> responsePage = PagedResult.of(List.of(), 0L, 0, 0, 20);

        given(partnerAssembler.toSearchCommand(any())).willReturn(new SearchPartnerCommand(null, null, null, null, false, 0, 20));
        given(partnerUseCase.searchPartners(any())).willReturn(summaryPage);
        given(partnerAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchPartnerRequest req = new SearchPartnerRequest(null, null, null, null, false, 0, 20);

        mockMvc.perform(post("/api/admin/partner/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── @PreAuthorize: CODE_MANAGE authority (PARTNER_MANAGE 없음) → 403 ─────

    @Test
    @WithMockUser(authorities = {"ROLE_USER", "CODE_MANAGE"})
    void search_withCodeManageOnly_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/partner/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isForbidden());
    }

    // ── @PreAuthorize: ROLE_ADMIN (authorities 없음) → 200 ───────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_withAdminRole_noAuthority_returns200() throws Exception {
        PagedResult<PartnerSummary> summaryPage = PagedResult.of(List.of(), 0L, 0, 0, 20);
        PagedResult<PartnerSummaryResponse> responsePage = PagedResult.of(List.of(), 0L, 0, 0, 20);

        given(partnerAssembler.toSearchCommand(any())).willReturn(new SearchPartnerCommand(null, null, null, null, false, 0, 20));
        given(partnerUseCase.searchPartners(any())).willReturn(summaryPage);
        given(partnerAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchPartnerRequest req = new SearchPartnerRequest(null, null, null, null, false, 0, 20);

        mockMvc.perform(post("/api/admin/partner/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
