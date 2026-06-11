package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.SearchMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailView;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.masterbl.port.in.MasterBlUseCase;
import com.freightos.common.security.GatewayProperties;
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
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(MasterBlController.class)
@ActiveProfiles("test")
class MasterBlControllerWebMvcTest {

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

    // SecurityConfig가 HeaderAuthenticationFilter를 등록하고, GatewayProperties를 주입받음
    // WebMvcTest 슬라이스에서 GatewayProperties 바인딩을 위해 Mock 등록
    @MockitoBean
    @SuppressWarnings("unused")
    private GatewayProperties gatewayProperties;

    // ── GET /api/master-bl/{id} ───────────────────────────────────────

    @Test
    @DisplayName("GET /api/master-bl/{id}: useCase.findMasterBlById 호출, 200 응답")
    void getMasterBlById_invokesFindMasterBlByIdUseCase() throws Exception {
        Long id = 1L;
        MasterBlDetailView mockView = mock(MasterBlDetailView.class);
        MasterBlDetailResponse mockResponse = mock(MasterBlDetailResponse.class);

        given(masterBlUseCase.findMasterBlById(id)).willReturn(mockView);
        given(masterBlAssembler.toDetail(mockView)).willReturn(mockResponse);

        mockMvc.perform(get("/api/master-bl/{id}", id)).andExpect(status().isOk());

        then(masterBlUseCase).should().findMasterBlById(id);
    }

    // ── GET /api/master-bl ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/master-bl?bound=EXP&page=0&size=50: 200 응답 및 응답 본문 검증")
    void searchMasterBls_happyPath_returns200() throws Exception {
        MasterBlSummaryResponse mockItem = mock(MasterBlSummaryResponse.class);
        PagedResult<MasterBlSummaryResponse> mockPage = PagedResult.of(
                List.of(mockItem), 1L, 1, 0, 50);

        SearchMasterBlCommand mockCmd = new SearchMasterBlCommand("EXP", null, null, null, null, null, null, null);
        given(masterBlAssembler.toSearchCommand(any())).willReturn(mockCmd);
        given(masterBlUseCase.searchMasterBls(any(), any())).willReturn(PagedResult.of(List.of(), 0L, 0, 0, 50));
        given(masterBlAssembler.toSummaryPage(any())).willReturn(mockPage);

        mockMvc.perform(get("/api/master-bl")
                        .param("bound", "EXP")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.page").value(0));

        then(masterBlUseCase).should().searchMasterBls(any(), any());
    }

    @Test
    @DisplayName("GET /api/master-bl: bound 파라미터 누락 → 400")
    void searchMasterBls_missingBound_returns400() throws Exception {
        mockMvc.perform(get("/api/master-bl")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/master-bl?bound=INVALID: 유효하지 않은 bound 값 → 400")
    void searchMasterBls_invalidBound_returns400() throws Exception {
        given(masterBlAssembler.toSearchCommand(any()))
                .willThrow(new IllegalArgumentException("No enum constant for INVALID"));

        mockMvc.perform(get("/api/master-bl")
                        .param("bound", "INVALID")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/master-bl/{id} NotFound ─────────────────────────────

    @Test
    @DisplayName("GET /api/master-bl/1: UseCase가 ResourceNotFoundException 던질 때 → 404, ProblemDetail 응답")
    void findMasterBlById_whenNotFound_returnsError() throws Exception {
        Long id = 1L;
        given(masterBlUseCase.findMasterBlById(id))
                .willThrow(new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND));

        mockMvc.perform(get("/api/master-bl/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(MessageCode.MASTER_BL_NOT_FOUND.message()));
    }

    // ── DELETE /api/master-bl/{id} ────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/master-bl/1: 200 응답 + MASTER_BL_DELETED 메시지")
    void deleteMasterBlById_happyPath_returns200() throws Exception {
        Long id = 1L;
        willDoNothing().given(masterBlUseCase).deleteMasterBlById(id);

        mockMvc.perform(delete("/api/master-bl/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MessageCode.MASTER_BL_DELETED.message()));

        then(masterBlUseCase).should().deleteMasterBlById(id);
    }

    // ── POST /api/master-bl ───────────────────────────────────────────

    @Test
    @DisplayName("POST /api/master-bl: jobDiv=SEA, bound=EXP happy path → 201 + Location 헤더 + data.id 검증")
    void createMasterBl_happyPath_returns201WithLocation() throws Exception {
        Long id = 1L;
        CreateMasterBlCommand mockCommand = mock(CreateMasterBlCommand.class);
        given(masterBlAssembler.toCreateCommand(any())).willReturn(mockCommand);
        given(masterBlUseCase.createMasterBl(any(CreateMasterBlCommand.class))).willReturn(id);

        mockMvc.perform(post("/api/master-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobDiv":"SEA","bound":"EXP",
                                  "mblNo":"MAEU-001","masterRefNo":"REF-001",
                                  "polCode":"KRPUS","podCode":"CNSHA",
                                  "etd":"20260601","eta":"20260610",
                                  "freightTerm":"PREPAID","shipmentType":"DIRECT",
                                  "operatorCode":"OPR01","teamCode":"TEAM01",
                                  "seaDetail":{
                                    "linerCode":"MAEU",
                                    "vesselName":"EVER GIVEN",
                                    "voyageNo":"001E"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/master-bl/1")))
                .andExpect(jsonPath("$.data.id").value(1L));

        then(masterBlUseCase).should().createMasterBl(any(CreateMasterBlCommand.class));
    }

    @Test
    @DisplayName("POST /api/master-bl: jobDiv 누락 → 400 validation 오류")
    void createMasterBl_missingJobDiv_returns400() throws Exception {
        mockMvc.perform(post("/api/master-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bound\":\"EXP\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/master-bl/{id} ───────────────────────────────────────

    @Test
    @DisplayName("PUT /api/master-bl/1: happy path → 200 + MASTER_BL_UPDATED 메시지")
    void updateMasterBl_happyPath_returns200() throws Exception {
        Long id = 1L;
        UpdateMasterBlCommand mockCommand = mock(UpdateMasterBlCommand.class);
        given(masterBlAssembler.toUpdateCommand(any())).willReturn(mockCommand);
        willDoNothing().given(masterBlUseCase).updateMasterBl(eq(id), eq(mockCommand));

        mockMvc.perform(put("/api/master-bl/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MessageCode.MASTER_BL_UPDATED.message()));

        then(masterBlUseCase).should().updateMasterBl(eq(id), any(UpdateMasterBlCommand.class));
    }

    @Test
    @DisplayName("PUT /api/master-bl/999: 존재하지 않는 id → 404")
    void updateMasterBl_whenNotFound_returns404() throws Exception {
        Long id = 999L;
        UpdateMasterBlCommand mockCommand = mock(UpdateMasterBlCommand.class);
        given(masterBlAssembler.toUpdateCommand(any())).willReturn(mockCommand);
        willThrow(new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND))
                .given(masterBlUseCase).updateMasterBl(eq(id), eq(mockCommand));

        mockMvc.perform(put("/api/master-bl/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(MessageCode.MASTER_BL_NOT_FOUND.message()));
    }
}
