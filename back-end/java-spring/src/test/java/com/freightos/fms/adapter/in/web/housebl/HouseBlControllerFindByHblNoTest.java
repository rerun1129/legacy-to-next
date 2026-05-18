package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.domain.housebl.enums.JobDiv;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /api/house-bl/find-by-hbl-no endpoint 전용 WebMvc 슬라이스 테스트.
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(HouseBlController.class)
@ActiveProfiles("test")
class HouseBlControllerFindByHblNoTest {

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

    private static final String URL = "/api/house-bl/find-by-hbl-no";

    // ── 정상 케이스 ───────────────────────────────────────────────────────

    @Test
    @DisplayName("findByHblNo: hblNo+jobDiv=SEA 1건 매칭 → 200, data size=1")
    void findByHblNo_singleMatch_returns200WithOneId() throws Exception {
        given(houseBlUseCase.findHouseBlKeysByHblNoExact(eq("HBL-001"), eq(JobDiv.SEA)))
                .willReturn(List.of(10L));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"HBL-001\",\"jobDiv\":\"SEA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0]").value(10L));

        then(houseBlUseCase).should().findHouseBlKeysByHblNoExact(eq("HBL-001"), eq(JobDiv.SEA));
    }

    @Test
    @DisplayName("findByHblNo: 매칭 없음 → 200, data size=0")
    void findByHblNo_noMatch_returns200WithEmptyList() throws Exception {
        given(houseBlUseCase.findHouseBlKeysByHblNoExact(any(), any())).willReturn(List.of());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"NO-MATCH\",\"jobDiv\":\"SEA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("findByHblNo: 중복 2건 → 200, data size=2")
    void findByHblNo_duplicateTwo_returns200WithTwoIds() throws Exception {
        given(houseBlUseCase.findHouseBlKeysByHblNoExact(eq("HBL-DUP"), eq(JobDiv.SEA)))
                .willReturn(List.of(20L, 10L));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"HBL-DUP\",\"jobDiv\":\"SEA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ── validation 케이스 ─────────────────────────────────────────────────

    @Test
    @DisplayName("findByHblNo: hblNo 빈값 → 400 validation 오류")
    void findByHblNo_blankHblNo_returns400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"\",\"jobDiv\":\"SEA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("findByHblNo: hblNo 51자 → 400 validation 오류")
    void findByHblNo_hblNoExceeds50Chars_returns400() throws Exception {
        String over50 = "A".repeat(51);
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"" + over50 + "\",\"jobDiv\":\"SEA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("findByHblNo: jobDiv 빈값 → 400 validation 오류")
    void findByHblNo_blankJobDiv_returns400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"HBL-001\",\"jobDiv\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("findByHblNo: jobDiv 유효하지 않은 값 → 400")
    void findByHblNo_invalidJobDiv_returns400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"HBL-001\",\"jobDiv\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());
    }
}
