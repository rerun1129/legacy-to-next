package com.freightos.admin.adapter.in.web.attributedefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.attributedefinition.port.in.AttributeDefinitionUseCase;
import com.freightos.admin.common.request.BulkDeleteByCodeRequest;
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

@WebMvcTest(controllers = AttributeDefinitionController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class AttributeDefinitionBulkDeleteControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttributeDefinitionUseCase attributeDefinitionUseCase;

    @MockitoBean
    private AttributeDefinitionAssembler attributeDefinitionAssembler;

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
        BulkDeleteByCodeRequest req = new BulkDeleteByCodeRequest(List.of("attr_key_1", "attr_key_2"));
        mockMvc.perform(delete("/api/admin/access/attribute/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── BTN_ADMIN_ACCESS_ATTRIBUTE_DELETE authority → 200 ────────────────────

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "BTN_ADMIN_ACCESS_ATTRIBUTE_DELETE"})
    void bulkDelete_validRequest_returns200() throws Exception {
        willDoNothing().given(attributeDefinitionUseCase).deleteAttributeDefinitionsByKeys(any());
        BulkDeleteByCodeRequest req = new BulkDeleteByCodeRequest(List.of("attr_key_1", "attr_key_2", "attr_key_3"));
        mockMvc.perform(delete("/api/admin/access/attribute/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
