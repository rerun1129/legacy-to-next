package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.masterbl.port.in.MasterBlUseCase;
import com.freightos.common.security.JwtAuthenticationFilter;
import com.freightos.common.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AIR Master B/L Controller WebMvcTest — Phase 2B.
 * §6.61: @MockitoBean Validator 추가 금지 — Validator 실 빈 사용.
 * 검증 그룹(AirMasterGroup/AirImpMasterGroup) 실 동작 확인 포함.
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(MasterBlController.class)
@ActiveProfiles("test")
class AirMasterBlControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MasterBlUseCase masterBlUseCase;

    @MockitoBean
    private MasterBlAssembler masterBlAssembler;

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

    // ── POST /api/master-bl (AIR EXP) happy path ──────────────────────

    @Test
    @DisplayName("POST /api/master-bl: jobDiv=AIR, bound=EXP happy path → 201 + Location 헤더 + data.id")
    void createMasterBl_airExp_happyPath_returns201WithLocation() throws Exception {
        Long id = 10L;
        CreateMasterBlCommand mockCommand = mock(CreateMasterBlCommand.class);
        given(masterBlAssembler.toCreateCommand(any())).willReturn(mockCommand);
        given(masterBlUseCase.createMasterBl(any(CreateMasterBlCommand.class))).willReturn(id);

        mockMvc.perform(post("/api/master-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobDiv":"AIR","bound":"EXP",
                                  "mblNo":"MAWB-001","masterRefNo":"REF-001",
                                  "polCode":"KRSEL","podCode":"JPNRT",
                                  "etd":"20251201","eta":"20251202",
                                  "freightTerm":"PREPAID",
                                  "operatorCode":"OPR01","teamCode":"TEAM01",
                                  "airDetail":{
                                    "airlineCode":"KE",
                                    "issueDate":"20251201",
                                    "issuePlace":"KRSEL",
                                    "signature":"AGENT"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/master-bl/10")))
                .andExpect(jsonPath("$.data.id").value(10L));

        then(masterBlUseCase).should().createMasterBl(any(CreateMasterBlCommand.class));
    }

    // ── POST /api/master-bl (AIR IMP) happy path ──────────────────────

    @Test
    @DisplayName("POST /api/master-bl: jobDiv=AIR, bound=IMP happy path → 201 + Location 헤더")
    void createMasterBl_airImp_happyPath_returns201() throws Exception {
        Long id = 11L;
        CreateMasterBlCommand mockCommand = mock(CreateMasterBlCommand.class);
        given(masterBlAssembler.toCreateCommand(any())).willReturn(mockCommand);
        given(masterBlUseCase.createMasterBl(any(CreateMasterBlCommand.class))).willReturn(id);

        mockMvc.perform(post("/api/master-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobDiv":"AIR","bound":"IMP",
                                  "mblNo":"MAWB-IMP-001","masterRefNo":"REF-IMP-001",
                                  "polCode":"JPNRT","podCode":"KRSEL",
                                  "etd":"20251201","eta":"20251202",
                                  "freightTerm":"COLLECT",
                                  "consigneeCode":"CONS01",
                                  "operatorCode":"OPR01","teamCode":"TEAM01",
                                  "airDetail":{
                                    "airlineCode":"OZ",
                                    "issueDate":"20251201",
                                    "issuePlace":"JPNRT",
                                    "signature":"AGENT"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/master-bl/11")));

        then(masterBlUseCase).should().createMasterBl(any(CreateMasterBlCommand.class));
    }

    // ── PUT /api/master-bl/{id} (AIR) happy path ──────────────────────

    @Test
    @DisplayName("PUT /api/master-bl/10: AIR Update happy path → 200 + MASTER_BL_UPDATED 메시지")
    void updateMasterBl_airHappyPath_returns200() throws Exception {
        Long id = 10L;
        UpdateMasterBlCommand mockCommand = mock(UpdateMasterBlCommand.class);
        given(masterBlAssembler.toUpdateCommand(any())).willReturn(mockCommand);
        willDoNothing().given(masterBlUseCase).updateMasterBl(eq(id), eq(mockCommand));

        mockMvc.perform(put("/api/master-bl/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobDiv":"AIR","bound":"EXP",
                                  "airDetail":{"airlineCode":"KE"}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MessageCode.MASTER_BL_UPDATED.message()));

        then(masterBlUseCase).should().updateMasterBl(eq(id), any(UpdateMasterBlCommand.class));
    }

    // ── GET /api/master-bl/{id} (AIR) happy path ──────────────────────

    @Test
    @DisplayName("GET /api/master-bl/10: AIR 단건 조회 → 200")
    void findMasterBlById_airHappyPath_returns200() throws Exception {
        Long id = 10L;
        MasterBlDetailResult mockResult = mock(MasterBlDetailResult.class);
        MasterBlDetailResponse mockResponse = mock(MasterBlDetailResponse.class);
        given(masterBlUseCase.findMasterBlById(id)).willReturn(mockResult);
        given(masterBlAssembler.toDetail(mockResult)).willReturn(mockResponse);

        mockMvc.perform(get("/api/master-bl/{id}", id)).andExpect(status().isOk());

        then(masterBlUseCase).should().findMasterBlById(id);
    }

    // ── DELETE /api/master-bl/{id} (AIR) happy path ───────────────────

    @Test
    @DisplayName("DELETE /api/master-bl/10: AIR 삭제 → 200 + MASTER_BL_DELETED 메시지")
    void deleteMasterBlById_airHappyPath_returns200() throws Exception {
        Long id = 10L;
        willDoNothing().given(masterBlUseCase).deleteMasterBlById(id);

        mockMvc.perform(delete("/api/master-bl/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MessageCode.MASTER_BL_DELETED.message()));

        then(masterBlUseCase).should().deleteMasterBlById(id);
    }

    // ── POST /api/master-bl: AIR 필수 필드 누락 → 400 ─────────────────

    @Test
    @DisplayName("POST /api/master-bl: AIR airDetail.airlineCode 누락 → 400 validation 오류")
    void createMasterBl_airMissingAirlineCode_returns400() throws Exception {
        mockMvc.perform(post("/api/master-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobDiv":"AIR","bound":"EXP",
                                  "mblNo":"MAWB-001","masterRefNo":"REF-001",
                                  "polCode":"KRSEL","podCode":"JPNRT",
                                  "etd":"20251201","eta":"20251202",
                                  "freightTerm":"PREPAID",
                                  "operatorCode":"OPR01","teamCode":"TEAM01",
                                  "airDetail":{
                                    "issueDate":"20251201",
                                    "issuePlace":"KRSEL",
                                    "signature":"AGENT"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/master-bl: AIR airDetail.issueDate 누락 → 400 validation 오류")
    void createMasterBl_airMissingIssueDate_returns400() throws Exception {
        mockMvc.perform(post("/api/master-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobDiv":"AIR","bound":"EXP",
                                  "mblNo":"MAWB-001","masterRefNo":"REF-001",
                                  "polCode":"KRSEL","podCode":"JPNRT",
                                  "etd":"20251201","eta":"20251202",
                                  "freightTerm":"PREPAID",
                                  "operatorCode":"OPR01","teamCode":"TEAM01",
                                  "airDetail":{
                                    "airlineCode":"KE",
                                    "issuePlace":"KRSEL",
                                    "signature":"AGENT"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/master-bl: AIR airDetail.issuePlace 누락 → 400 validation 오류")
    void createMasterBl_airMissingIssuePlace_returns400() throws Exception {
        mockMvc.perform(post("/api/master-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobDiv":"AIR","bound":"EXP",
                                  "mblNo":"MAWB-001","masterRefNo":"REF-001",
                                  "polCode":"KRSEL","podCode":"JPNRT",
                                  "etd":"20251201","eta":"20251202",
                                  "freightTerm":"PREPAID",
                                  "operatorCode":"OPR01","teamCode":"TEAM01",
                                  "airDetail":{
                                    "airlineCode":"KE",
                                    "issueDate":"20251201",
                                    "signature":"AGENT"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/master-bl: AIR airDetail.signature 누락 → 400 validation 오류")
    void createMasterBl_airMissingSignature_returns400() throws Exception {
        mockMvc.perform(post("/api/master-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobDiv":"AIR","bound":"EXP",
                                  "mblNo":"MAWB-001","masterRefNo":"REF-001",
                                  "polCode":"KRSEL","podCode":"JPNRT",
                                  "etd":"20251201","eta":"20251202",
                                  "freightTerm":"PREPAID",
                                  "operatorCode":"OPR01","teamCode":"TEAM01",
                                  "airDetail":{
                                    "airlineCode":"KE",
                                    "issueDate":"20251201",
                                    "issuePlace":"KRSEL"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/master-bl/10: NotFound → 404 ─────────────────────────

    @Test
    @DisplayName("GET /api/master-bl/999: AIR 존재하지 않는 id → 404 ProblemDetail 응답")
    void findMasterBlById_airNotFound_returns404() throws Exception {
        Long id = 999L;
        given(masterBlUseCase.findMasterBlById(id))
                .willThrow(new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND));

        mockMvc.perform(get("/api/master-bl/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(MessageCode.MASTER_BL_NOT_FOUND.message()));
    }
}
