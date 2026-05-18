package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.application.masterbl.port.in.MasterBlUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /api/master-bl/find-by-mbl-no endpoint 전용 WebMvc 슬라이스 테스트.
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(MasterBlController.class)
@ActiveProfiles("test")
class MasterBlControllerFindByMblNoTest {

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

    private static final String URL = "/api/master-bl/find-by-mbl-no";

    // ── 정상 케이스 ───────────────────────────────────────────────────────

    @Test
    @DisplayName("findByMblNo: mblNo 1건 매칭 → 200, data size=1")
    void findByMblNo_singleMatch_returns200WithOneId() throws Exception {
        given(masterBlUseCase.findMasterBlKeysByMblNoExact(eq("MBL-001")))
                .willReturn(List.of(10L));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mblNo\":\"MBL-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0]").value(10L));

        then(masterBlUseCase).should().findMasterBlKeysByMblNoExact(eq("MBL-001"));
    }

    @Test
    @DisplayName("findByMblNo: 매칭 없음 → 200, data size=0")
    void findByMblNo_noMatch_returns200WithEmptyList() throws Exception {
        given(masterBlUseCase.findMasterBlKeysByMblNoExact(eq("NO-MATCH")))
                .willReturn(List.of());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mblNo\":\"NO-MATCH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("findByMblNo: 중복 2건 → 200, data size=2")
    void findByMblNo_duplicateTwo_returns200WithTwoIds() throws Exception {
        given(masterBlUseCase.findMasterBlKeysByMblNoExact(eq("MBL-DUP")))
                .willReturn(List.of(20L, 10L));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mblNo\":\"MBL-DUP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ── validation 케이스 ─────────────────────────────────────────────────

    @Test
    @DisplayName("findByMblNo: mblNo 빈값 → 400 validation 오류")
    void findByMblNo_blankMblNo_returns400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mblNo\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
