package com.freightos.admin.adapter.in.web.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.subscription.dto.SaveSubscriptionChangesRequest;
import com.freightos.admin.adapter.in.web.subscription.dto.SubscriptionItemResponse;
import com.freightos.admin.application.subscription.command.SaveSubscriptionChangesCommand;
import com.freightos.admin.application.subscription.port.in.SubscriptionUseCase;
import com.freightos.admin.application.subscription.projection.SubscriptionSummary;
import com.freightos.admin.common.response.SaveChangesResult;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SubscriptionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubscriptionUseCase subscriptionUseCase;

    @MockitoBean
    private SubscriptionAssembler subscriptionAssembler;

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
    void getSubscriptions_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/subscriber/1/subscription"))
                .andExpect(status().isUnauthorized());
    }

    // ── MENU_ADMIN_SUBSCRIBER_LIST authority → 구독 목록 200 ─────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_SUBSCRIBER_LIST")
    void getSubscriptions_authenticated_returns200() throws Exception {
        SubscriptionSummary summary = new SubscriptionSummary(10L, 1L, "FMS",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), true,
                LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 1, 0, 0));
        SubscriptionItemResponse response = new SubscriptionItemResponse(10L, 1L, "FMS",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), true,
                LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 1, 0, 0));

        given(subscriptionUseCase.getSubscriptionsBySubscriberId(1L)).willReturn(List.of(summary));
        given(subscriptionAssembler.toResponse(any())).willReturn(response);

        mockMvc.perform(get("/api/admin/subscriber/1/subscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].moduleCode").value("FMS"));
    }

    // ── BTN_ADMIN_SUBSCRIBER_SUBSCRIPTION_SAVE authority → save-changes 200 ──

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_SUBSCRIBER_SUBSCRIPTION_SAVE")
    void saveChanges_authenticated_returns200() throws Exception {
        given(subscriptionAssembler.toSaveChangesCommand(eq(1L), any())).willReturn(
                new SaveSubscriptionChangesCommand(1L, List.of(), List.of(), List.of()));
        given(subscriptionUseCase.saveSubscriptionChanges(any())).willReturn(new SaveChangesResult(0, 0, 0));

        SaveSubscriptionChangesRequest req = new SaveSubscriptionChangesRequest(List.of(), List.of(), List.of());

        mockMvc.perform(post("/api/admin/subscriber/1/subscription/save-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── MENU_ADMIN_SUBSCRIBER_LIST (구독 save) → 403 ─────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_SUBSCRIBER_LIST")
    void saveChanges_wrongAuthority_returns403() throws Exception {
        SaveSubscriptionChangesRequest req = new SaveSubscriptionChangesRequest(List.of(), List.of(), List.of());

        mockMvc.perform(post("/api/admin/subscriber/1/subscription/save-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ── 권한 없음 → getSubscriptions 403 ─────────────────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CODE_LIST")
    void getSubscriptions_wrongAuthority_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/subscriber/1/subscription"))
                .andExpect(status().isForbidden());
    }
}
