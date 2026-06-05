package com.freightos.bms.adapter.in.web.financialdocument;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.bms.adapter.in.web.financialdocument.dto.FreightLineDetailResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.SearchFinancialDocumentRequest;
import com.freightos.bms.application.financialdocument.FinancialDocumentSearchView;
import com.freightos.bms.application.financialdocument.FreightLineDetailView;
import com.freightos.bms.application.financialdocument.port.in.FinancialDocumentGroupUseCase;
import com.freightos.bms.application.financialdocument.port.in.FinancialDocumentUseCase;
import com.freightos.common.security.JwtAuthenticationFilter;
import com.freightos.common.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FinancialDocumentController 신규 엔드포인트 @WebMvcTest.
 * - POST /api/bms/financial-documents/search
 * - GET  /api/bms/financial-documents/{id}/lines
 * 기존 엔드포인트 테스트는 별도 파일로 관리한다.
 */
@WebMvcTest(FinancialDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinancialDocumentSearchControllerTest {

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

    // SecurityConfig에서 JwtTokenProvider 의존 — WebMvcTest 슬라이스에서는 Mock 필요
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /search — documentTypes IN INVOICE 결과 1건 반환")
    @WithMockUser
    void searchDocuments_returnsPage() throws Exception {
        FinancialDocumentSearchView view = new FinancialDocumentSearchView(
            1L, "INV-2406-00001", "INVOICE", "20240601", "CREATED",
            "CUST001", "고객명",
            BigDecimal.valueOf(1000), BigDecimal.valueOf(1100000), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(800),
            "20240601", "SEA", "해상팀", "admin_user", "홍길동", null,
            "HOUSE", 123L, "SEA", "EXP", "HBL-0001", "20240601", "20240610"
        );
        Page<FinancialDocumentSearchView> page = new PageImpl<>(List.of(view), PageRequest.of(0, 20), 1);

        given(financialDocumentUseCase.searchDocuments(any(), any())).willReturn(page);
        given(assembler.toCriteria(any())).willCallRealMethod();
        given(assembler.toPageResponse(any())).willCallRealMethod();
        given(assembler.toSearchResponse(any())).willCallRealMethod();

        SearchFinancialDocumentRequest request = new SearchFinancialDocumentRequest(
            List.of("INVOICE"), null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            0, 20
        );

        mockMvc.perform(post("/api/bms/financial-documents/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].documentNo").value("INV-2406-00001"));
    }

    @Test
    @DisplayName("POST /search — documentTypes 빈 리스트면 400")
    @WithMockUser
    void searchDocuments_emptyDocumentTypes_returnsBadRequest() throws Exception {
        SearchFinancialDocumentRequest request = new SearchFinancialDocumentRequest(
            List.of(), null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            0, 20
        );

        mockMvc.perform(post("/api/bms/financial-documents/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{id}/lines — 라인 목록 반환")
    @WithMockUser
    void findDocumentLines_returnsLines() throws Exception {
        FreightLineDetailView lineView = new FreightLineDetailView(
            10L, 5L, "SELLING", "INVOICE", "FRTCODE", "운임명",
            BigDecimal.ONE, BigDecimal.valueOf(1000), "BL",
            "USD", BigDecimal.valueOf(1300), BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1300000), BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.valueOf(0.000769), BigDecimal.valueOf(769.23),
            "CUST001", "고객명",
            "TAXABLE", null, null, null, null, "20240601", 1L
        );
        FreightLineDetailResponse detailResponse = new FreightLineDetailResponse(
            10L, 5L, "SELLING", "INVOICE", "FRTCODE", "운임명",
            BigDecimal.ONE, BigDecimal.valueOf(1000), "BL",
            "USD", BigDecimal.valueOf(1300), BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1300000), BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.valueOf(0.000769), BigDecimal.valueOf(769.23),
            "CUST001", "고객명",
            "TAXABLE", null, null, null, null, "20240601", 1L
        );

        given(financialDocumentUseCase.findDocumentLines(1L)).willReturn(List.of(lineView));
        given(assembler.toDetailResponse(lineView)).willReturn(detailResponse);

        mockMvc.perform(get("/api/bms/financial-documents/1/lines"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].freightLineId").value(10));
    }
}
