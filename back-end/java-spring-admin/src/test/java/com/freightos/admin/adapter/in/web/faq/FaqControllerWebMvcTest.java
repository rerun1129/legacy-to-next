package com.freightos.admin.adapter.in.web.faq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.faq.dto.CreateFaqRequest;
import com.freightos.admin.adapter.in.web.faq.dto.FaqSummaryResponse;
import com.freightos.admin.adapter.in.web.faq.dto.SearchFaqRequest;
import com.freightos.admin.adapter.in.web.faqcategory.FaqCategoryAssembler;
import com.freightos.admin.adapter.in.web.faqcategory.FaqCategoryController;
import com.freightos.admin.adapter.in.web.faqcategory.dto.CreateFaqCategoryRequest;
import com.freightos.admin.adapter.in.web.faqcategory.dto.FaqCategorySummaryResponse;
import com.freightos.admin.adapter.in.web.faqcategory.dto.SearchFaqCategoryRequest;
import com.freightos.admin.application.faq.command.CreateFaqCommand;
import com.freightos.admin.application.faq.command.SearchFaqCommand;
import com.freightos.admin.application.faq.port.in.FaqUseCase;
import com.freightos.admin.application.faq.projection.FaqSummary;
import com.freightos.admin.application.faqcategory.command.CreateFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.command.SearchFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.port.in.FaqCategoryUseCase;
import com.freightos.admin.application.faqcategory.projection.FaqCategorySummary;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({FaqController.class, FaqCategoryController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class FaqControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FaqUseCase faqUseCase;

    @MockitoBean
    private FaqAssembler faqAssembler;

    @MockitoBean
    private FaqCategoryUseCase faqCategoryUseCase;

    @MockitoBean
    private FaqCategoryAssembler faqCategoryAssembler;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    // ── FAQ search 미인증 → 401 ───────────────────────────────────────────────

    @Test
    void faq_search_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/cms/faq/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── FAQ search ADMIN → 200 ────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void faq_search_adminAuthenticated_returns200() throws Exception {
        FaqSummaryResponse summaryResponse = new FaqSummaryResponse(1L, 1L, "질문1", 0, true, null, null);
        PagedResult<FaqSummary> summaryPage = PagedResult.of(List.of(), 1L, 1, 0, 20);
        PagedResult<FaqSummaryResponse> responsePage = PagedResult.of(List.of(summaryResponse), 1L, 1, 0, 20);

        given(faqAssembler.toSearchCommand(any())).willReturn(new SearchFaqCommand(null, null, "ACTIVE", 0, 20));
        given(faqUseCase.searchFaqs(any())).willReturn(summaryPage);
        given(faqAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchFaqRequest req = new SearchFaqRequest(null, null, "ACTIVE", 0, 20);

        mockMvc.perform(post("/api/admin/cms/faq/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].faqId").value(1L))
                .andExpect(jsonPath("$.data.content[0].question").value("질문1"));
    }

    // ── FAQ create → 201 + Location + data.id ────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void faq_create_returns201WithLocationAndId() throws Exception {
        given(faqAssembler.toCreateCommand(any())).willReturn(new CreateFaqCommand(1L, "질문", "답변", 0, true));
        given(faqUseCase.createFaq(any())).willReturn(42L);

        CreateFaqRequest req = new CreateFaqRequest(1L, "질문", "답변", 0, true);

        mockMvc.perform(post("/api/admin/cms/faq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(42));
    }

    // ── FAQ create blank question → 400 ──────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void faq_create_blankQuestion_returns400() throws Exception {
        String body = """
                {"faqCategoryId":1,"question":"","answer":"답변","sortOrder":0,"active":true}
                """;
        mockMvc.perform(post("/api/admin/cms/faq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── FAQ create blank answer → 400 ────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void faq_create_blankAnswer_returns400() throws Exception {
        String body = """
                {"faqCategoryId":1,"question":"질문","answer":"","sortOrder":0,"active":true}
                """;
        mockMvc.perform(post("/api/admin/cms/faq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── FAQ search CMS_MANAGE authority → 200 ────────────────────────────────

    @Test
    @WithMockUser(authorities = {"ROLE_USER", "CMS_MANAGE"})
    void faq_search_withCmsManageAuthority_returns200() throws Exception {
        PagedResult<FaqSummary> summaryPage = PagedResult.of(List.of(), 0L, 0, 0, 20);
        PagedResult<FaqSummaryResponse> responsePage = PagedResult.of(List.of(), 0L, 0, 0, 20);

        given(faqAssembler.toSearchCommand(any())).willReturn(new SearchFaqCommand(null, null, null, 0, 20));
        given(faqUseCase.searchFaqs(any())).willReturn(summaryPage);
        given(faqAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchFaqRequest req = new SearchFaqRequest(null, null, null, 0, 20);

        mockMvc.perform(post("/api/admin/cms/faq/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── FAQ search CODE_MANAGE only → 403 ────────────────────────────────────

    @Test
    @WithMockUser(authorities = {"ROLE_USER", "CODE_MANAGE"})
    void faq_search_withCodeManageOnly_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/cms/faq/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isForbidden());
    }

    // ── FAQ delete ADMIN → 200 ────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void faq_delete_adminAuthenticated_returns200() throws Exception {
        willDoNothing().given(faqUseCase).deleteFaq(any());

        mockMvc.perform(delete("/api/admin/cms/faq/1"))
                .andExpect(status().isOk());
    }

    // ── FaqCategory search ADMIN → 200 ───────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void faqCategory_search_adminAuthenticated_returns200() throws Exception {
        FaqCategorySummaryResponse summaryResponse = new FaqCategorySummaryResponse(1L, "카테고리A", 0, true, null, null);
        PagedResult<FaqCategorySummary> summaryPage = PagedResult.of(List.of(), 1L, 1, 0, 20);
        PagedResult<FaqCategorySummaryResponse> responsePage = PagedResult.of(List.of(summaryResponse), 1L, 1, 0, 20);

        given(faqCategoryAssembler.toSearchCommand(any())).willReturn(new SearchFaqCategoryCommand(null, "ACTIVE", 0, 20));
        given(faqCategoryUseCase.searchFaqCategories(any())).willReturn(summaryPage);
        given(faqCategoryAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchFaqCategoryRequest req = new SearchFaqCategoryRequest(null, "ACTIVE", 0, 20);

        mockMvc.perform(post("/api/admin/cms/faq-category/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].faqCategoryId").value(1L))
                .andExpect(jsonPath("$.data.content[0].name").value("카테고리A"));
    }

    // ── FaqCategory create → 201 + Location + data.id ────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void faqCategory_create_returns201WithLocationAndId() throws Exception {
        given(faqCategoryAssembler.toCreateCommand(any())).willReturn(new CreateFaqCategoryCommand("신규카테고리", 0, true));
        given(faqCategoryUseCase.createFaqCategory(any())).willReturn(55L);

        CreateFaqCategoryRequest req = new CreateFaqCategoryRequest("신규카테고리", 0, true);

        mockMvc.perform(post("/api/admin/cms/faq-category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(55));
    }
}
