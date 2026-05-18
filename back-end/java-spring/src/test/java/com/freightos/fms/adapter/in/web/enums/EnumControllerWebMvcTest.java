package com.freightos.fms.adapter.in.web.enums;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.adapter.in.web.enums.dto.EnumMapResponse;
import com.freightos.fms.adapter.in.web.enums.dto.EnumOptionResponse;
import com.freightos.fms.application.enums.EnumRegistry;
import com.freightos.fms.application.enums.port.in.EnumQueryResult;
import com.freightos.fms.application.enums.port.in.EnumQueryUseCase;
import com.freightos.common.security.JwtAuthenticationFilter;
import com.freightos.common.security.JwtTokenProvider;
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
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(EnumController.class)
@ActiveProfiles("test")
class EnumControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnumQueryUseCase enumQueryUseCase;

    @MockitoBean
    private EnumAssembler enumAssembler;

    @MockitoBean
    private EnumRegistry enumRegistry;

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

    @Test
    @DisplayName("GET /api/enums/Per → 200 + Cache-Control + ETag 헤더 + JSON body")
    void getByName_happyPath_returns200WithCacheHeaders() throws Exception {
        List<EnumOptionResponse> responses = List.of(
                new EnumOptionResponse("SHP", "Ship", "Ship"));
        given(enumQueryUseCase.getByName("Per")).willReturn(List.of());
        given(enumAssembler.toResponse(List.of())).willReturn(responses);
        given(enumRegistry.getEtag()).willReturn("abc123");

        mockMvc.perform(get("/api/enums/{name}", "Per"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "public, max-age=3600"))
                .andExpect(header().string("ETag", "\"abc123\""))
                .andExpect(jsonPath("$.data[0].code").value("SHP"))
                .andExpect(jsonPath("$.data[0].label").value("Ship"));
    }

    @Test
    @DisplayName("GET /api/enums/Unknown → 404 ProblemDetail (GlobalExceptionHandler 처리)")
    void getByName_unknownName_returns404() throws Exception {
        given(enumQueryUseCase.getByName("Unknown"))
                .willThrow(new ResourceNotFoundException("EnumRegistry", "Unknown"));

        mockMvc.perform(get("/api/enums/{name}", "Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/enums?names=Per,Bound → 200 + EnumMapResponse body")
    void getByNames_happyPath_returns200() throws Exception {
        EnumQueryResult queryResult = new EnumQueryResult(
                Map.of("Per", List.of(), "Bound", List.of()),
                List.of());
        EnumMapResponse mapResponse = new EnumMapResponse(
                Map.of("Per", List.of(new EnumOptionResponse("SHP", "Ship", "Ship")),
                        "Bound", List.of(new EnumOptionResponse("EXP", "EXP", null))),
                List.of());
        given(enumQueryUseCase.getByNames(List.of("Per", "Bound"))).willReturn(queryResult);
        given(enumAssembler.toMapResponse(queryResult)).willReturn(mapResponse);
        given(enumRegistry.getEtag()).willReturn("abc123");

        mockMvc.perform(get("/api/enums")
                        .param("names", "Per", "Bound"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "public, max-age=3600"))
                .andExpect(header().string("ETag", "\"abc123\""))
                .andExpect(jsonPath("$.data.enums.Per[0].code").value("SHP"))
                .andExpect(jsonPath("$.data.enums.Bound[0].code").value("EXP"))
                .andExpect(jsonPath("$.data.notFound").isEmpty());
    }

    @Test
    @DisplayName("GET /api/enums?names=Per,Unknown → 200 + notFound=[\"Unknown\"]")
    void getByNames_partialNotFound_returns200WithNotFound() throws Exception {
        EnumQueryResult queryResult = new EnumQueryResult(
                Map.of("Per", List.of()),
                List.of("Unknown"));
        EnumMapResponse mapResponse = new EnumMapResponse(
                Map.of("Per", List.of(new EnumOptionResponse("SHP", "Ship", "Ship"))),
                List.of("Unknown"));
        given(enumQueryUseCase.getByNames(List.of("Per", "Unknown"))).willReturn(queryResult);
        given(enumAssembler.toMapResponse(queryResult)).willReturn(mapResponse);
        given(enumRegistry.getEtag()).willReturn("abc123");

        mockMvc.perform(get("/api/enums")
                        .param("names", "Per", "Unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notFound[0]").value("Unknown"));
    }
}
