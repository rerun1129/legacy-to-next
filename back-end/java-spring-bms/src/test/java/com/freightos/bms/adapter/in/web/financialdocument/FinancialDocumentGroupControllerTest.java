package com.freightos.bms.adapter.in.web.financialdocument;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.bms.adapter.in.web.financialdocument.dto.ApplyGroupingRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.ApplyGroupingResponse;
import com.freightos.bms.application.financialdocument.GroupResult;
import com.freightos.bms.application.financialdocument.port.in.FinancialDocumentGroupUseCase;
import com.freightos.bms.application.financialdocument.port.in.FinancialDocumentUseCase;
import com.freightos.common.security.JwtAuthenticationFilter;
import com.freightos.common.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /api/bms/financial-documents/group @WebMvcTest.
 */
@WebMvcTest(FinancialDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinancialDocumentGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FinancialDocumentUseCase financialDocumentUseCase;

    @MockitoBean
    private FinancialDocumentGroupUseCase financialDocumentGroupUseCase;

    @MockitoBean
    private FinancialDocumentAssembler assembler;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /group — 200 OK + groupFinancialNo 반환")
    @WithMockUser
    void applyGrouping_returns200WithGroupNo() throws Exception {
        GroupResult groupResult = new GroupResult("GI260600001", List.of(1L, 2L), List.of());
        ApplyGroupingResponse response = new ApplyGroupingResponse("GI260600001", List.of(1L, 2L), List.of());

        given(financialDocumentGroupUseCase.applyGrouping(any())).willReturn(groupResult);
        given(assembler.toCommand(any(ApplyGroupingRequest.class))).willCallRealMethod();
        given(assembler.toResponse(groupResult)).willReturn(response);

        ApplyGroupingRequest request = new ApplyGroupingRequest(List.of(1L, 2L), List.of(1L, 2L));

        mockMvc.perform(post("/api/bms/financial-documents/group")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.groupFinancialNo").value("GI260600001"))
            .andExpect(jsonPath("$.data.groupedDocumentIds[0]").value(1))
            .andExpect(jsonPath("$.data.groupedDocumentIds[1]").value(2));
    }

    @Test
    @DisplayName("POST /group — groupedDocumentIds null이면 400")
    @WithMockUser
    void applyGrouping_nullGroupedIds_returns400() throws Exception {
        String body = "{\"groupedDocumentIds\": null, \"scopeDocumentIds\": [1]}";

        mockMvc.perform(post("/api/bms/financial-documents/group")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }
}
