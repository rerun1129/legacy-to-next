package com.freightos.fms.adapter.in.web.nonbl;

import com.freightos.fms.application.nonbl.port.in.NonBlUseCase;
import com.freightos.common.security.JwtAuthenticationFilter;
import com.freightos.common.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * bound 필드 @NotBlank 검증 테스트.
 * DDL 상 bound VARCHAR(3) NOT NULL 이며 EXP/IMP 식별 구분값이므로
 * Create/Update 모두 누락·공백 시 400 응답을 보장한다.
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(NonBlController.class)
@ActiveProfiles("test")
class NonBlBoundValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NonBlUseCase nonBlUseCase;

    @MockitoBean
    private NonBlAssembler nonBlAssembler;

    // @EnableJpaAuditing이 FmsApplication에 선언되어 WebMvcTest 슬라이스에서도 JpaMetamodelMappingContext를 요구함
    @MockitoBean
    @SuppressWarnings("unused")
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // SecurityConfig가 JwtAuthenticationFilter를 등록하고, JwtAuthenticationFilter는 JwtTokenProvider를 주입받음
    // WebMvcTest 슬라이스는 @Component인 JwtTokenProvider를 스캔하지 않으므로 Mock 등록으로 컨텍스트 로딩 보완
    @MockitoBean
    @SuppressWarnings("unused")
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    @SuppressWarnings("unused")
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ── 공통 최소 필수 필드 (bound 제외) ────────────────────────────────────
    private static final String CREATE_REQUIRED_WITHOUT_BOUND = """
            {
              "hblNo": "NB-001",
              "workDivision": "SEA",
              "polCode": "KRPUS",
              "podCode": "USLAX",
              "etd": "20260101",
              "eta": "20260201",
              "actualCustomerCode": "CUST01",
              "operatorCode": "OP01",
              "teamCode": "TEAM01"
            }
            """;

    private static final String UPDATE_REQUIRED_WITHOUT_BOUND = """
            {
              "hblNo": "NB-001"
            }
            """;

    // ── POST /api/non-bl : bound 누락/공백 → 400 ────────────────────────────

    @Test
    @DisplayName("POST /api/non-bl: bound 누락 → 400 validation 오류")
    void createNonBl_missingBound_returns400() throws Exception {
        mockMvc.perform(post("/api/non-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_REQUIRED_WITHOUT_BOUND))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/non-bl: bound 공백 → 400 validation 오류")
    void createNonBl_blankBound_returns400() throws Exception {
        String body = CREATE_REQUIRED_WITHOUT_BOUND.replace("{", "{\"bound\":\"\",");
        mockMvc.perform(post("/api/non-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/non-bl/{id} : bound 누락/공백 → 400 ────────────────────────

    @Test
    @DisplayName("PUT /api/non-bl/1: bound 누락 → 400 validation 오류")
    void updateNonBl_missingBound_returns400() throws Exception {
        mockMvc.perform(put("/api/non-bl/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_REQUIRED_WITHOUT_BOUND))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/non-bl/1: bound 공백 → 400 validation 오류")
    void updateNonBl_blankBound_returns400() throws Exception {
        String body = UPDATE_REQUIRED_WITHOUT_BOUND.replace("{", "{\"bound\":\"\",");
        mockMvc.perform(put("/api/non-bl/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
