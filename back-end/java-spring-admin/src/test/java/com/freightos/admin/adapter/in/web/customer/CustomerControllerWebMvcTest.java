package com.freightos.admin.adapter.in.web.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.customer.dto.CreateCustomerRequest;
import com.freightos.admin.adapter.in.web.customer.dto.CustomerDetailResponse;
import com.freightos.admin.adapter.in.web.customer.dto.CustomerSummaryResponse;
import com.freightos.admin.adapter.in.web.customer.dto.SearchCustomerRequest;
import com.freightos.admin.application.customer.command.CreateCustomerCommand;
import com.freightos.admin.application.customer.command.SearchCustomerCommand;
import com.freightos.admin.application.customer.port.in.CustomerUseCase;
import com.freightos.admin.application.customer.projection.CustomerSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.security.JpaUserDetailsService;
import com.freightos.admin.common.security.JwtAuthenticationFilter;
import com.freightos.admin.common.security.JwtTokenProvider;
import com.freightos.admin.common.security.SecurityConfig;
import com.freightos.admin.domain.customer.entity.CustomerType;
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

@WebMvcTest(controllers = CustomerController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class CustomerControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerUseCase customerUseCase;

    @MockitoBean
    private CustomerAssembler customerAssembler;

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
        mockMvc.perform(post("/api/admin/customer/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── MENU_ADMIN_CUSTOMER_LIST authority → 200 ──────────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CUSTOMER_LIST")
    void search_authenticated_returns200() throws Exception {
        CustomerSummaryResponse summaryResponse = new CustomerSummaryResponse(
                1L, "CUS-001", CustomerType.FORWARDER, "글로벌 포워더",
                null, null, null, null, null, null, null, null, null,
                true, null, LocalDateTime.of(2024, 1, 1, 0, 0));
        PagedResult<CustomerSummary> summaryPage = PagedResult.of(List.of(), 1L, 1, 0, 20);
        PagedResult<CustomerSummaryResponse> responsePage = PagedResult.of(List.of(summaryResponse), 1L, 1, 0, 20);

        given(customerAssembler.toSearchCommand(any())).willReturn(new SearchCustomerCommand(null, null, null, null, 0, 20));
        given(customerUseCase.searchCustomers(any())).willReturn(summaryPage);
        given(customerAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchCustomerRequest req = new SearchCustomerRequest(null, null, null, null, 0, 20);

        mockMvc.perform(post("/api/admin/customer/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].customerCode").value("CUS-001"));
    }

    // ── BTN_ADMIN_CUSTOMER_LIST_CREATE authority → 201 ────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CUSTOMER_LIST_CREATE")
    void create_returns201WithLocationAndId() throws Exception {
        given(customerAssembler.toCreateCommand(any())).willReturn(
                new CreateCustomerCommand("CUS-001", CustomerType.FORWARDER, "테스트 포워더",
                        null, null, null, null, null, null, null, null, null, true));
        given(customerUseCase.createCustomer(any())).willReturn(42L);

        CreateCustomerRequest req = new CreateCustomerRequest(
                "CUS-001", CustomerType.FORWARDER, "테스트 포워더",
                null, null, null, null, null, null, null, null, null, Boolean.TRUE);

        mockMvc.perform(post("/api/admin/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(42));
    }

    // ── customerCode @NotBlank → 400 ─────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CUSTOMER_LIST_CREATE")
    void create_blankCustomerCode_returns400() throws Exception {
        String body = """
                {"customerCode":"","customerType":"FORWARDER","name":"테스트","active":true}
                """;
        mockMvc.perform(post("/api/admin/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── name @NotBlank → 400 ─────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CUSTOMER_LIST_CREATE")
    void create_blankName_returns400() throws Exception {
        String body = """
                {"customerCode":"CUS-001","customerType":"FORWARDER","name":"","active":true}
                """;
        mockMvc.perform(post("/api/admin/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── BTN_ADMIN_CUSTOMER_LIST_DELETE authority → 200 ────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CUSTOMER_LIST_DELETE")
    void delete_returns200() throws Exception {
        willDoNothing().given(customerUseCase).deleteCustomer(any());

        mockMvc.perform(delete("/api/admin/customer/1"))
                .andExpect(status().isOk());
    }

    // ── MENU_ADMIN_CUSTOMER_LIST authority → getById 200 ─────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CUSTOMER_LIST")
    void getById_returns200WithDetail() throws Exception {
        CustomerDetailResponse detail = new CustomerDetailResponse(
                1L, "CUS-001", CustomerType.FORWARDER, "글로벌 포워더", "Global Forwarder",
                null, null, null, null, null, null, null, null, true, null,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 2, 0, 0),
                "admin", "admin");

        given(customerUseCase.getCustomerById(1L)).willReturn(null);
        given(customerAssembler.toDetail(any())).willReturn(detail);

        mockMvc.perform(get("/api/admin/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.customerCode").value("CUS-001"))
                .andExpect(jsonPath("$.data.customerType").value("FORWARDER"));
    }

    // ── @PreAuthorize: MENU_ADMIN_CUSTOMER_LIST authority → 200 ─────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CUSTOMER_LIST")
    void search_withCustomerListMenuAuthority_returns200() throws Exception {
        PagedResult<CustomerSummary> summaryPage = PagedResult.of(List.of(), 0L, 0, 0, 20);
        PagedResult<CustomerSummaryResponse> responsePage = PagedResult.of(List.of(), 0L, 0, 0, 20);

        given(customerAssembler.toSearchCommand(any())).willReturn(new SearchCustomerCommand(null, null, null, null, 0, 20));
        given(customerUseCase.searchCustomers(any())).willReturn(summaryPage);
        given(customerAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchCustomerRequest req = new SearchCustomerRequest(null, null, null, null, 0, 20);

        mockMvc.perform(post("/api/admin/customer/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── @PreAuthorize: 다른 authority (MENU_ADMIN_CODE_LIST) → 403 ───────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CODE_LIST")
    void search_withCodeListMenuOnly_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/customer/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isForbidden());
    }
}
