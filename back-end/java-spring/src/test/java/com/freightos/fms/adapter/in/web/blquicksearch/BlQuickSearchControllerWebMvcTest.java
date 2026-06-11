package com.freightos.fms.adapter.in.web.blquicksearch;

import com.freightos.common.security.GatewayProperties;
import com.freightos.fms.adapter.in.web.blquicksearch.dto.BlQuickSearchItemResponse;
import com.freightos.fms.application.blquicksearch.command.BlQuickSearchCommand;
import com.freightos.fms.application.blquicksearch.port.in.BlQuickSearchUseCase;
import com.freightos.fms.application.blquicksearch.projection.BlQuickSearchSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(BlQuickSearchController.class)
@ActiveProfiles("test")
class BlQuickSearchControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BlQuickSearchUseCase blQuickSearchUseCase;

    @MockitoBean
    private BlQuickSearchAssembler blQuickSearchAssembler;

    // @EnableJpaAuditing이 FmsApplication에 선언되어 WebMvcTest 슬라이스에서도 JpaMetamodelMappingContext를 요구함
    @MockitoBean
    @SuppressWarnings("unused")
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // SecurityConfig가 HeaderAuthenticationFilter를 등록하고, GatewayProperties를 주입받음
    // WebMvcTest 슬라이스에서 GatewayProperties 바인딩을 위해 Mock 등록
    @MockitoBean
    @SuppressWarnings("unused")
    private GatewayProperties gatewayProperties;

    @Test
    @DisplayName("GET /api/bl/quick-search/autocomplete: 200 + data 배열 반환")
    void autocomplete_happyPath_returns200WithData() throws Exception {
        BlQuickSearchSummary summary = new BlQuickSearchSummary(1L, "HOUSE", "BL-001", "SEA", "EXP", "SHIP01", "KRPUS", "USLAX", "20260101");
        BlQuickSearchItemResponse responseItem = BlQuickSearchItemResponse.from(summary);

        given(blQuickSearchAssembler.toCommand(any())).willReturn(
                new BlQuickSearchCommand("BL-001", "SEA", null, null, null, null, null, null, null, null, null, null, null, null));
        given(blQuickSearchUseCase.quickSearch(any())).willReturn(List.of(summary));
        given(blQuickSearchAssembler.toResponseList(any())).willReturn(List.of(responseItem));

        mockMvc.perform(get("/api/bl/quick-search/autocomplete")
                        .param("q", "BL-001")
                        .param("jobDiv", "SEA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].blType").value("HOUSE"))
                .andExpect(jsonPath("$.data[0].blNo").value("BL-001"))
                .andExpect(jsonPath("$.data[0].label").isString());
    }

    @Test
    @DisplayName("GET /api/bl/quick-search/autocomplete: 결과 없으면 빈 배열 반환")
    void autocomplete_emptyResult_returnsEmptyArray() throws Exception {
        given(blQuickSearchAssembler.toCommand(any())).willReturn(
                new BlQuickSearchCommand(null, null, null, null, null, null, null, null, null, null, null, null, null, null));
        given(blQuickSearchUseCase.quickSearch(any())).willReturn(List.of());
        given(blQuickSearchAssembler.toResponseList(any())).willReturn(List.of());

        mockMvc.perform(get("/api/bl/quick-search/autocomplete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /api/bl/quick-search/autocomplete: q 파라미터 없어도 200 반환 (빈 q 허용)")
    void autocomplete_noQParam_returns200() throws Exception {
        given(blQuickSearchAssembler.toCommand(any())).willReturn(
                new BlQuickSearchCommand(null, null, null, null, null, null, null, null, null, null, null, null, null, null));
        given(blQuickSearchUseCase.quickSearch(any())).willReturn(List.of());
        given(blQuickSearchAssembler.toResponseList(any())).willReturn(List.of());

        mockMvc.perform(get("/api/bl/quick-search/autocomplete"))
                .andExpect(status().isOk());
    }
}
