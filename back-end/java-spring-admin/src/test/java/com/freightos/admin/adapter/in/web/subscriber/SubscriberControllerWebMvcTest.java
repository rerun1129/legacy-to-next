package com.freightos.admin.adapter.in.web.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.subscriber.dto.CreateSubscriberRequest;
import com.freightos.admin.adapter.in.web.subscriber.dto.SearchSubscriberRequest;
import com.freightos.admin.adapter.in.web.subscriber.dto.SubscriberDetailResponse;
import com.freightos.admin.adapter.in.web.subscriber.dto.SubscriberSummaryResponse;
import com.freightos.admin.application.subscriber.command.CreateSubscriberCommand;
import com.freightos.admin.application.subscriber.command.SearchSubscriberCommand;
import com.freightos.admin.application.subscriber.port.in.SubscriberUseCase;
import com.freightos.admin.application.subscriber.projection.SubscriberSummary;
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

@WebMvcTest(SubscriberController.class)
@Import({SecurityConfig.class, HeaderAuthenticationFilter.class})
class SubscriberControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubscriberUseCase subscriberUseCase;

    @MockitoBean
    private SubscriberAssembler subscriberAssembler;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    // ── 미인증 → 401 ──────────────────────────────────────────────────────────

    @Test
    void search_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/subscriber/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── MENU_ADMIN_SUBSCRIBER_LIST authority → search 200 ─────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_SUBSCRIBER_LIST")
    void search_authenticated_returns200() throws Exception {
        SubscriberSummaryResponse summaryResponse = new SubscriberSummaryResponse(
                1L, "SUB001", "테스트구독사", null, null, null, null, null, null,
                true, null, LocalDateTime.of(2025, 1, 1, 0, 0));
        PagedResult<SubscriberSummary> summaryPage = PagedResult.of(List.of(), 1L, 1, 0, 20);
        PagedResult<SubscriberSummaryResponse> responsePage = PagedResult.of(List.of(summaryResponse), 1L, 1, 0, 20);

        given(subscriberAssembler.toSearchCommand(any())).willReturn(new SearchSubscriberCommand(null, null, "ALL", 0, 20));
        given(subscriberUseCase.searchSubscribers(any())).willReturn(summaryPage);
        given(subscriberAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchSubscriberRequest req = new SearchSubscriberRequest(null, null, null, 0, 20);

        mockMvc.perform(post("/api/admin/subscriber/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].subscriberCode").value("SUB001"));
    }

    // ── BTN_ADMIN_SUBSCRIBER_LIST_SAVE authority → create 201 ────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_SUBSCRIBER_LIST_SAVE")
    void create_returns201WithLocationAndId() throws Exception {
        given(subscriberAssembler.toCreateCommand(any())).willReturn(
                new CreateSubscriberCommand("SUB001", "테스트구독사", null, null, null, null, null, null, true));
        given(subscriberUseCase.createSubscriber(any())).willReturn(42L);

        CreateSubscriberRequest req = new CreateSubscriberRequest("SUB001", "테스트구독사", null, null, null, null, null, null, Boolean.TRUE);

        mockMvc.perform(post("/api/admin/subscriber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(42));
    }

    // ── subscriberCode @NotBlank → 400 ───────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_SUBSCRIBER_LIST_SAVE")
    void create_blankSubscriberCode_returns400() throws Exception {
        String body = """
                {"subscriberCode":"","name":"테스트","active":true}
                """;
        mockMvc.perform(post("/api/admin/subscriber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── name @NotBlank → 400 ─────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_SUBSCRIBER_LIST_SAVE")
    void create_blankName_returns400() throws Exception {
        String body = """
                {"subscriberCode":"SUB001","name":"","active":true}
                """;
        mockMvc.perform(post("/api/admin/subscriber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── BTN_ADMIN_SUBSCRIBER_LIST_SAVE authority → delete 200 ────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_SUBSCRIBER_LIST_SAVE")
    void deleteById_returns200() throws Exception {
        willDoNothing().given(subscriberUseCase).deleteSubscriber(any());

        mockMvc.perform(delete("/api/admin/subscriber/1"))
                .andExpect(status().isOk());
    }

    // ── MENU_ADMIN_SUBSCRIBER_LIST authority → getById 200 ───────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_SUBSCRIBER_LIST")
    void getById_returns200() throws Exception {
        SubscriberDetailResponse detail = new SubscriberDetailResponse(
                1L, "SUB001", "테스트구독사", null, null, null, null, null, null, true, null,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 2, 0, 0),
                "admin", "admin");

        given(subscriberUseCase.getSubscriberById(1L)).willReturn(null);
        given(subscriberAssembler.toDetail(any())).willReturn(detail);

        mockMvc.perform(get("/api/admin/subscriber/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.subscriberCode").value("SUB001"));
    }

    // ── 권한 없음 (MENU_ADMIN_CODE_LIST) → 403 ───────────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CODE_LIST")
    void search_wrongAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/subscriber/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isForbidden());
    }
}
