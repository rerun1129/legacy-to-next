package com.freightos.fms.adapter.in.web.truckbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.truckbl.port.in.TruckBlSearchUseCase;
import com.freightos.fms.application.truckbl.port.in.TruckBlUseCase;
import com.freightos.fms.common.response.MessageCode;
import org.hamcrest.Matchers;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TruckBlController.class)
@ActiveProfiles("test")
class TruckBlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TruckBlUseCase truckBlUseCase;

    @MockitoBean
    private TruckBlSearchUseCase truckBlSearchUseCase;

    @MockitoBean
    private TruckBlAssembler truckBlAssembler;

    // @EnableJpaAuditing이 FmsApplication에 선언되어 WebMvcTest 슬라이스에서도 JpaMetamodelMappingContext를 요구함
    @MockitoBean
    @SuppressWarnings("unused")
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // ── POST /api/truck-bl ────────────────────────────────────────────────

    private static final String VALID_CREATE_JSON = """
            {
              "hblNo": "TB-001",
              "bound": "EXP",
              "polCode": "KRPUS",
              "podCode": "USLAX",
              "etd": "20260101",
              "eta": "20260201",
              "actualCustomerCode": "CUST01",
              "operatorCode": "OP01",
              "teamCode": "TEAM01",
              "salesManCode": "SALES01"
            }
            """;

    @Test
    @DisplayName("POST /api/truck-bl: 정상 요청 → 201, data.id 일치, Location 헤더 포함")
    void createTruckBl_happyPath_returns201WithIdAndLocation() throws Exception {
        Long createdId = 42L;
        given(truckBlAssembler.toCreateCommand(any())).willReturn(null);
        given(truckBlUseCase.createTruckBl(any())).willReturn(createdId);

        mockMvc.perform(post("/api/truck-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CREATE_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.endsWith("/api/truck-bl/42")))
                .andExpect(jsonPath("$.data.id").value(createdId))
                .andExpect(jsonPath("$.message").value(MessageCode.TRUCK_BL_CREATED.message()));

        then(truckBlUseCase).should().createTruckBl(any());
    }

    // ── PUT /api/truck-bl/{id} ───────────────────────────────────────────

    private static final String VALID_UPDATE_JSON = """
            {
              "bound": "EXP",
              "polCode": "KRPUS",
              "podCode": "USLAX",
              "etd": "20260101",
              "eta": "20260201",
              "actualCustomerCode": "CUST01",
              "operatorCode": "OP01",
              "teamCode": "TEAM01",
              "salesManCode": "SALES01"
            }
            """;

    @Test
    @DisplayName("PUT /api/truck-bl/1: 정상 요청 → 200, data null, message 포함")
    void updateTruckBl_happyPath_returns200WithNullData() throws Exception {
        Long id = 1L;
        given(truckBlAssembler.toUpdateCommand(any())).willReturn(null);
        willDoNothing().given(truckBlUseCase).updateTruckBl(eq(id), any());

        mockMvc.perform(put("/api/truck-bl/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value(MessageCode.TRUCK_BL_UPDATED.message()));

        then(truckBlUseCase).should().updateTruckBl(eq(id), any());
    }

    @Test
    @DisplayName("PUT /api/truck-bl/999: 미존재 id → 404, GlobalExceptionHandler 처리")
    void updateTruckBl_whenNotFound_returns404() throws Exception {
        Long id = 999L;
        given(truckBlAssembler.toUpdateCommand(any())).willReturn(null);
        willThrow(new ResourceNotFoundException(MessageCode.TRUCK_BL_NOT_FOUND))
                .given(truckBlUseCase).updateTruckBl(eq(id), any());

        mockMvc.perform(put("/api/truck-bl/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(MessageCode.TRUCK_BL_NOT_FOUND.message()));
    }

    // ── DELETE /api/truck-bl/{id} ────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/truck-bl/1: 정상 요청 → 200, TRUCK_BL_DELETED 메시지")
    void deleteTruckBlById_happyPath_returns200() throws Exception {
        Long id = 1L;
        willDoNothing().given(truckBlUseCase).deleteTruckBlById(id);

        mockMvc.perform(delete("/api/truck-bl/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MessageCode.TRUCK_BL_DELETED.message()));

        then(truckBlUseCase).should().deleteTruckBlById(id);
    }

    // ── PUT /api/truck-bl/{id}/hbl-no ────────────────────────────────────

    @Test
    @DisplayName("PUT /api/truck-bl/1/hbl-no: 정상 hblNo → 200, useCase.changeTruckBlHblNo 호출")
    void changeTruckBlHblNo_happyPath_returns200() throws Exception {
        Long id = 1L;
        willDoNothing().given(truckBlUseCase).changeTruckBlHblNo(eq(id), any(ChangeHouseBlNoCommand.class));

        mockMvc.perform(put("/api/truck-bl/{id}/hbl-no", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"NEW-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(MessageCode.TRUCK_BL_UPDATED.message()));

        then(truckBlUseCase).should().changeTruckBlHblNo(eq(id), any(ChangeHouseBlNoCommand.class));
    }

    @Test
    @DisplayName("PUT /api/truck-bl/1/hbl-no: hblNo 빈값 → 400 validation 오류")
    void changeTruckBlHblNo_blankHblNo_returns400() throws Exception {
        mockMvc.perform(put("/api/truck-bl/{id}/hbl-no", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/truck-bl/1/hbl-no: hblNo 51자 → 400 validation 오류")
    void changeTruckBlHblNo_hblNoExceeds50Chars_returns400() throws Exception {
        String over50 = "A".repeat(51);
        mockMvc.perform(put("/api/truck-bl/{id}/hbl-no", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"" + over50 + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/truck-bl/999/hbl-no: 미존재 id → 404, GlobalExceptionHandler 처리")
    void changeTruckBlHblNo_whenNotFound_returns404() throws Exception {
        Long id = 999L;
        willThrow(new ResourceNotFoundException(MessageCode.TRUCK_BL_NOT_FOUND))
                .given(truckBlUseCase).changeTruckBlHblNo(eq(id), any(ChangeHouseBlNoCommand.class));

        mockMvc.perform(put("/api/truck-bl/{id}/hbl-no", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"NEW-001\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(MessageCode.TRUCK_BL_NOT_FOUND.message()));
    }

    // ── POST /api/truck-bl/find-by-hbl-no ───────────────────────────────

    @Test
    @DisplayName("POST /api/truck-bl/find-by-hbl-no: 정상 요청 → 200, id 목록 반환")
    void findTruckBlsByHblNoExact_happyPath_returns200() throws Exception {
        given(truckBlUseCase.findTruckBlKeysByHblNoExact("TB-001"))
                .willReturn(java.util.List.of(1L, 2L));

        mockMvc.perform(post("/api/truck-bl/find-by-hbl-no")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hblNo\":\"TB-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value(1))
                .andExpect(jsonPath("$.data[1]").value(2));
    }
}
