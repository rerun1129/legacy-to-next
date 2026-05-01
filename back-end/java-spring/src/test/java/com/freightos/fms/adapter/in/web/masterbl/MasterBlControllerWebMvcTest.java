package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.MasterBlDetail;
import com.freightos.fms.domain.masterbl.port.in.MasterBlUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
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
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    // ── GET /api/master-bl/{id} ───────────────────────────────────────

    @Test
    @DisplayName("GET /api/master-bl/{id}: useCase.findMasterBlDetailById 호출 (findMasterBlById 회귀 차단), 200 응답")
    void getMasterBlById_invokesFindMasterBlDetailByIdUseCase() throws Exception {
        Long id = 1L;
        MasterBlDetail mockDetail = mock(MasterBlDetail.class);
        MasterBlDetailResponse mockResponse = mock(MasterBlDetailResponse.class);

        given(masterBlUseCase.findMasterBlDetailById(id)).willReturn(mockDetail);
        given(masterBlAssembler.toDetail(mockDetail)).willReturn(mockResponse);

        mockMvc.perform(get("/api/master-bl/{id}", id)).andExpect(status().isOk());

        then(masterBlUseCase).should().findMasterBlDetailById(id);
        then(masterBlUseCase).should(never()).findMasterBlById(any());
    }

    // ── GET /api/master-bl ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/master-bl?bound=EXP: 200 응답 및 응답 본문 검증")
    void getMasterBlsByBound_happyPath_returns200() throws Exception {
        MasterBlSummaryResponse mockItem = mock(MasterBlSummaryResponse.class);
        PagedResult<MasterBlSummaryResponse> mockPage = PagedResult.of(
                List.of(mockItem), 1L, 1, 0, 50);

        given(masterBlUseCase.getMasterBlsByBound(eq(Bound.EXP), any()))
                .willReturn(mock(PagedResult.class));
        given(masterBlAssembler.toSummaryPage(any())).willReturn(mockPage);

        mockMvc.perform(get("/api/master-bl")
                        .param("bound", "EXP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.page").value(0));

        then(masterBlUseCase).should().getMasterBlsByBound(eq(Bound.EXP), any());
    }

    @Test
    @DisplayName("GET /api/master-bl: bound 파라미터 누락 → 400")
    void getMasterBlsByBound_missingBound_returns400() throws Exception {
        mockMvc.perform(get("/api/master-bl"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/master-bl?bound=INVALID: 유효하지 않은 bound 값 → 400")
    void getMasterBlsByBound_invalidBound_returns400() throws Exception {
        mockMvc.perform(get("/api/master-bl")
                        .param("bound", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/master-bl/{id} NotFound ─────────────────────────────

    @Test
    @DisplayName("GET /api/master-bl/1: UseCase가 ResourceNotFoundException 던질 때 → 404, ProblemDetail 응답")
    void findMasterBlById_whenNotFound_returnsError() throws Exception {
        Long id = 1L;
        given(masterBlUseCase.findMasterBlDetailById(id))
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
}
