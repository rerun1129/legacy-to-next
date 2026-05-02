package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.port.in.HouseBlUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@WebMvcTest(HouseBlController.class)
@ActiveProfiles("test")
class HouseBlControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HouseBlUseCase houseBlUseCase;

    @MockitoBean
    private HouseBlAssembler houseBlAssembler;

    // @EnableJpaAuditing이 FmsApplication에 선언되어 WebMvcTest 슬라이스에서도 JpaMetamodelMappingContext를 요구함
    @MockitoBean
    @SuppressWarnings("unused")
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // ── GET /api/house-bl ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/house-bl?jobDiv=SEA&bound=EXP: 200 응답 및 응답 본문 검증")
    void getHouseBlsByJobDivAndBound_happyPath_returns200() throws Exception {
        HouseBlSummaryResponse mockItem = mock(HouseBlSummaryResponse.class);
        PagedResult<HouseBlSummaryResponse> mockPage = PagedResult.of(
                List.of(mockItem), 1L, 1, 0, 10);

        given(houseBlUseCase.getHouseBlsByJobDivAndBound(eq(JobDiv.SEA), eq(Bound.EXP), any()))
                .willReturn(mock(PagedResult.class));
        given(houseBlAssembler.toSummaryPage(any())).willReturn(mockPage);

        mockMvc.perform(get("/api/house-bl")
                        .param("jobDiv", "SEA")
                        .param("bound", "EXP")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));

        then(houseBlUseCase).should().getHouseBlsByJobDivAndBound(eq(JobDiv.SEA), eq(Bound.EXP), any());
    }

    @Test
    @DisplayName("GET /api/house-bl?jobDiv=INVALID: 유효하지 않은 jobDiv 값 → 400")
    void getHouseBlsByJobDivAndBound_invalidJobDiv_returns400() throws Exception {
        mockMvc.perform(get("/api/house-bl")
                        .param("jobDiv", "INVALID")
                        .param("bound", "EXP"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/house-bl?jobDiv=SEA: bound 파라미터 누락 → 400")
    void getHouseBlsByJobDivAndBound_missingBound_returns400() throws Exception {
        mockMvc.perform(get("/api/house-bl")
                        .param("jobDiv", "SEA"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/house-bl/{id} ────────────────────────────────────────

    @Test
    @DisplayName("GET /api/house-bl/1: 200 응답")
    void getHouseBlById_happyPath_returns200() throws Exception {
        Long id = 1L;
        HouseBlDetailResponse mockResponse = mock(HouseBlDetailResponse.class);

        given(houseBlUseCase.findHouseBlById(id)).willReturn(mock(
                com.freightos.fms.domain.housebl.entity.HouseBl.class));
        given(houseBlAssembler.toDetail(any())).willReturn(mockResponse);

        mockMvc.perform(get("/api/house-bl/{id}", id))
                .andExpect(status().isOk());

        then(houseBlUseCase).should().findHouseBlById(id);
    }

    @Test
    @DisplayName("GET /api/house-bl/1: UseCase가 ResourceNotFoundException 던질 때 → 404, ProblemDetail 응답")
    void getHouseBlById_whenNotFound_returns404OrError() throws Exception {
        Long id = 1L;
        given(houseBlUseCase.findHouseBlById(id))
                .willThrow(new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND));

        mockMvc.perform(get("/api/house-bl/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(MessageCode.HOUSE_BL_NOT_FOUND.message()));
    }

    // ── DELETE /api/house-bl/{id} ─────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/house-bl/1: 200 응답 + HOUSE_BL_DELETED 메시지")
    void deleteHouseBlById_happyPath_returns200() throws Exception {
        Long id = 1L;
        willDoNothing().given(houseBlUseCase).deleteHouseBlById(id);

        mockMvc.perform(delete("/api/house-bl/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MessageCode.HOUSE_BL_DELETED.message()));

        then(houseBlUseCase).should().deleteHouseBlById(id);
    }

    @Test
    @DisplayName("DELETE /api/house-bl/1: UseCase가 ResourceNotFoundException 던질 때 → 404, ProblemDetail 응답")
    void deleteHouseBlById_whenNotFound_returnsError() throws Exception {
        Long id = 1L;
        willThrow(new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND))
                .given(houseBlUseCase).deleteHouseBlById(id);

        mockMvc.perform(delete("/api/house-bl/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(MessageCode.HOUSE_BL_NOT_FOUND.message()));
    }

    // ── POST /api/house-bl ────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/house-bl: jobDiv=SEA, bound=EXP happy path → 201 + Location 헤더")
    void createHouseBl_happyPath_returns201WithLocation() throws Exception {
        HouseBl mockEntity = mock(HouseBl.class);
        HouseBlDetailResponse mockResponse = mock(HouseBlDetailResponse.class);
        given(mockEntity.getId()).willReturn(1L);
        given(houseBlAssembler.toEntity(any())).willReturn(mockEntity);
        given(houseBlUseCase.save(mockEntity)).willReturn(mockEntity);
        given(houseBlAssembler.toDetail(mockEntity)).willReturn(mockResponse);

        mockMvc.perform(post("/api/house-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobDiv\":\"SEA\",\"bound\":\"EXP\",\"shipmentType\":\"HOUSE\",\"freightTerm\":\"PREPAID\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/house-bl/1")));

        then(houseBlUseCase).should().save(mockEntity);
    }

    @Test
    @DisplayName("POST /api/house-bl: jobDiv 누락 → 400 validation 오류")
    void createHouseBl_missingJobDiv_returns400() throws Exception {
        mockMvc.perform(post("/api/house-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bound\":\"EXP\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/house-bl/{id} ────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/house-bl/1: happy path → 200")
    void updateHouseBl_happyPath_returns200() throws Exception {
        Long id = 1L;
        HouseBl mockEntity = mock(HouseBl.class);
        HouseBlDetailResponse mockResponse = mock(HouseBlDetailResponse.class);
        given(houseBlUseCase.findHouseBlById(id)).willReturn(mockEntity);
        given(houseBlUseCase.save(mockEntity)).willReturn(mockEntity);
        given(houseBlAssembler.toDetail(mockEntity)).willReturn(mockResponse);

        mockMvc.perform(put("/api/house-bl/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        then(houseBlUseCase).should().findHouseBlById(id);
        then(houseBlUseCase).should().save(mockEntity);
    }

    @Test
    @DisplayName("PUT /api/house-bl/999: 존재하지 않는 id → 404")
    void updateHouseBl_whenNotFound_returns404() throws Exception {
        Long id = 999L;
        given(houseBlUseCase.findHouseBlById(id))
                .willThrow(new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND));

        mockMvc.perform(put("/api/house-bl/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(MessageCode.HOUSE_BL_NOT_FOUND.message()));
    }
}
