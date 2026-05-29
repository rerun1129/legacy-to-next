package com.freightos.admin.adapter.in.web.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.customer.port.in.CustomerUseCase;
import com.freightos.admin.common.request.BulkDeleteRequest;
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
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class CustomerBulkDeleteControllerWebMvcTest {

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
    void bulkDelete_unauthenticated_returns401() throws Exception {
        BulkDeleteRequest req = new BulkDeleteRequest(List.of(1L, 2L));
        mockMvc.perform(delete("/api/admin/customer/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── BTN_ADMIN_CUSTOMER_LIST_SAVE authority → 200 ──────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_CUSTOMER_LIST_SAVE")
    void bulkDelete_validRequest_returns200() throws Exception {
        willDoNothing().given(customerUseCase).deleteCustomers(any());
        BulkDeleteRequest req = new BulkDeleteRequest(List.of(1L, 2L, 3L));
        mockMvc.perform(delete("/api/admin/customer/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
