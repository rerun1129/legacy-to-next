package com.freightos.fms.adapter.in.web.nonbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.nonbl.port.in.NonBlUseCase;
import com.freightos.fms.common.response.MessageCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.hamcrest.Matchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NonBlController.class)
@ActiveProfiles("test")
class NonBlControllerTest {

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

    // ── POST /api/non-bl ──────────────────────────────────────────────────

    private static final String VALID_CREATE_JSON = """
            {
              "hblNo": "NB-001",
              "bound": "EXP",
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

    @Test
    @DisplayName("POST /api/non-bl: 정상 요청 → 201, data.id 일치, Location 헤더 포함")
    void createNonBl_happyPath_returns201WithIdAndLocation() throws Exception {
        Long createdId = 42L;
        given(nonBlAssembler.toCreateCommand(any())).willReturn(null);
        given(nonBlUseCase.createNonBl(any())).willReturn(createdId);

        mockMvc.perform(post("/api/non-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CREATE_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.endsWith("/api/non-bl/42")))
                .andExpect(jsonPath("$.data.id").value(createdId))
                .andExpect(jsonPath("$.message").value(MessageCode.NON_BL_CREATED.message()));

        then(nonBlUseCase).should().createNonBl(any());
    }

    // ── PUT /api/non-bl/{id} ─────────────────────────────────────────────

    private static final String VALID_UPDATE_JSON = """
            {
              "bound": "EXP",
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

    @Test
    @DisplayName("PUT /api/non-bl/1: 정상 요청 → 200, data null, message 포함")
    void updateNonBl_happyPath_returns200WithNullData() throws Exception {
        Long id = 1L;
        given(nonBlAssembler.toUpdateCommand(any())).willReturn(null);
        willDoNothing().given(nonBlUseCase).updateNonBl(eq(id), any());

        mockMvc.perform(put("/api/non-bl/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value(MessageCode.NON_BL_UPDATED.message()));

        then(nonBlUseCase).should().updateNonBl(eq(id), any());
    }

    @Test
    @DisplayName("PUT /api/non-bl/999: 미존재 id → 404, GlobalExceptionHandler 처리")
    void updateNonBl_whenNotFound_returns404() throws Exception {
        Long id = 999L;
        given(nonBlAssembler.toUpdateCommand(any())).willReturn(null);
        willThrow(new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND))
                .given(nonBlUseCase).updateNonBl(eq(id), any());

        mockMvc.perform(put("/api/non-bl/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(MessageCode.NON_BL_NOT_FOUND.message()));
    }

    // ── PUT /api/non-bl/{id}/hbl-no ──────────────────────────────────────

    @Test
    @DisplayName("PUT /api/non-bl/1/hbl-no: 정상 hblNo → 200, useCase.changeNonBlHblNo 호출")
    void changeHblNo_happyPath_returns200() throws Exception {
        Long id = 1L;
        willDoNothing().given(nonBlUseCase).changeNonBlHblNo(eq(id), any(ChangeHouseBlNoCommand.class));

        mockMvc.perform(put("/api/non-bl/{id}/hbl-no", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"NEW-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MessageCode.NON_BL_UPDATED.message()));

        then(nonBlUseCase).should().changeNonBlHblNo(eq(id), any(ChangeHouseBlNoCommand.class));
    }

    @Test
    @DisplayName("PUT /api/non-bl/1/hbl-no: hblNo 빈값 → 400 validation 오류")
    void changeHblNo_blankHblNo_returns400() throws Exception {
        mockMvc.perform(put("/api/non-bl/{id}/hbl-no", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/non-bl/1/hbl-no: hblNo 51자 → 400 validation 오류")
    void changeHblNo_hblNoExceeds50Chars_returns400() throws Exception {
        String over50 = "A".repeat(51);
        mockMvc.perform(put("/api/non-bl/{id}/hbl-no", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"" + over50 + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/non-bl/999/hbl-no: 미존재 id → 404, GlobalExceptionHandler 처리")
    void changeHblNo_whenNotFound_returns404() throws Exception {
        Long id = 999L;
        willThrow(new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND))
                .given(nonBlUseCase).changeNonBlHblNo(eq(id), any(ChangeHouseBlNoCommand.class));

        mockMvc.perform(put("/api/non-bl/{id}/hbl-no", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"NEW-001\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(MessageCode.NON_BL_NOT_FOUND.message()));
    }
}
