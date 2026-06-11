package com.freightos.admin.adapter.in.web.notice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.notice.dto.CreateNoticeRequest;
import com.freightos.admin.adapter.in.web.notice.dto.NoticeSummaryResponse;
import com.freightos.admin.adapter.in.web.notice.dto.SearchNoticeRequest;
import com.freightos.admin.application.notice.command.CreateNoticeCommand;
import com.freightos.admin.application.notice.command.SearchNoticeCommand;
import com.freightos.admin.application.notice.port.in.NoticeUseCase;
import com.freightos.admin.application.notice.projection.NoticeSummary;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoticeController.class)
@Import({ SecurityConfig.class, HeaderAuthenticationFilter.class })
class NoticeControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoticeUseCase noticeUseCase;

    @MockitoBean
    private NoticeAssembler noticeAssembler;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    // ── 미인증 → 401 ──────────────────────────────────────────────────────────

    @Test
    void search_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/cms/notice/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── MENU_ADMIN_CMS_NOTICE_LIST authority → 200 ────────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CMS_NOTICE_LIST")
    void search_authenticatedWithNoticeMenuAuthority_returns200() throws Exception {
        NoticeSummaryResponse summaryResponse = new NoticeSummaryResponse(1L, "공지 제목", false, true, null, null, null, null);
        PagedResult<NoticeSummary> summaryPage = PagedResult.of(List.of(), 1L, 1, 0, 20);
        PagedResult<NoticeSummaryResponse> responsePage = PagedResult.of(List.of(summaryResponse), 1L, 1, 0, 20);

        given(noticeAssembler.toSearchCommand(any())).willReturn(new SearchNoticeCommand(null, null, "ACTIVE", null, 0, 20));
        given(noticeUseCase.searchNotices(any())).willReturn(summaryPage);
        given(noticeAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchNoticeRequest req = new SearchNoticeRequest(null, null, "ACTIVE", null, 0, 20);

        mockMvc.perform(post("/api/admin/cms/notice/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].title").value("공지 제목"));
    }

    // ── BTN_ADMIN_CMS_NOTICE_LIST_CREATE authority → 201 ─────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CMS_NOTICE_LIST_CREATE")
    void create_returns201WithLocationAndId() throws Exception {
        given(noticeAssembler.toCreateCommand(any())).willReturn(
                new CreateNoticeCommand("공지 제목", "공지 내용", false, true, null, null));
        given(noticeUseCase.createNotice(any())).willReturn(42L);

        CreateNoticeRequest req = new CreateNoticeRequest("공지 제목", "공지 내용", Boolean.FALSE, Boolean.TRUE, null, null);

        mockMvc.perform(post("/api/admin/cms/notice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(42));
    }

    // ── title @NotBlank → 400 ────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CMS_NOTICE_LIST_CREATE")
    void create_blankTitle_returns400() throws Exception {
        String body = """
                {"title":"","content":"내용","pinned":false,"active":true}
                """;
        mockMvc.perform(post("/api/admin/cms/notice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── content @NotBlank → 400 ──────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CMS_NOTICE_LIST_CREATE")
    void create_blankContent_returns400() throws Exception {
        String body = """
                {"title":"제목","content":"","pinned":false,"active":true}
                """;
        mockMvc.perform(post("/api/admin/cms/notice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── BTN_ADMIN_CMS_NOTICE_LIST_DELETE authority → 200 ─────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CMS_NOTICE_LIST_DELETE")
    void delete_returns200() throws Exception {
        willDoNothing().given(noticeUseCase).deleteNotice(any());

        mockMvc.perform(delete("/api/admin/cms/notice/1"))
                .andExpect(status().isOk());
    }

    // ── @PreAuthorize: MENU_ADMIN_CMS_NOTICE_LIST authority → 200 ────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CMS_NOTICE_LIST")
    void search_withNoticeListMenuAuthority_returns200() throws Exception {
        PagedResult<NoticeSummary> summaryPage = PagedResult.of(List.of(), 0L, 0, 0, 20);
        PagedResult<NoticeSummaryResponse> responsePage = PagedResult.of(List.of(), 0L, 0, 0, 20);

        given(noticeAssembler.toSearchCommand(any())).willReturn(new SearchNoticeCommand(null, null, null, null, 0, 20));
        given(noticeUseCase.searchNotices(any())).willReturn(summaryPage);
        given(noticeAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchNoticeRequest req = new SearchNoticeRequest(null, null, null, null, 0, 20);

        mockMvc.perform(post("/api/admin/cms/notice/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── @PreAuthorize: 다른 authority (MENU_ADMIN_CODE_LIST) → 403 ───────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CODE_LIST")
    void search_withCodeListMenuOnly_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/cms/notice/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isForbidden());
    }
}
