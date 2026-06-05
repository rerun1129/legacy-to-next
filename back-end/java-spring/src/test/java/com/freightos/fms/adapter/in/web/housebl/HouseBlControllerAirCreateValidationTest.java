package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import com.freightos.common.security.JwtAuthenticationFilter;
import com.freightos.common.security.JwtTokenProvider;
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

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /api/house-bl AIR EXP/IMP 검증 시나리오 전용 WebMvc 슬라이스 테스트.
 *
 * AirGroup 필수 12개 + AirImpGroup 추가 1개(consigneeCode) + nested airDetail.airlineCode(@Valid cascade) 검증.
 * AirImpGroup extends AirGroup 구조이므로 IMP 검증 시 AirGroup 어노테이션도 함께 트리거된다.
 */
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(HouseBlController.class)
@ActiveProfiles("test")
class HouseBlControllerAirCreateValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HouseBlUseCase houseBlUseCase;

    @MockitoBean
    private HouseBlAssembler houseBlAssembler;

    @MockitoBean
    @SuppressWarnings("unused")
    private HouseBlFreightAssembler houseBlFreightAssembler;

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

    // ── AIR EXP 필수 필드 누락 시나리오 ───────────────────────────────────

    @Test
    @DisplayName("POST /api/house-bl: AIR EXP — hblNo(HAWB NO) 누락 → 400")
    void createHouseBl_air_exp_missing_hblNo_returnsValidationError() throws Exception {
        mockMvc.perform(post("/api/house-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(airExpBody(null, "AL001", "SALESMAN01")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/house-bl: AIR EXP — airDetail.airlineCode 누락(nested @Valid cascade) → 400")
    void createHouseBl_air_exp_missing_airlineCode_returnsValidationError() throws Exception {
        mockMvc.perform(post("/api/house-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(airExpBody("HAWB-001", null, "SALESMAN01")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/house-bl: AIR EXP — salesManCode 누락 → 400")
    void createHouseBl_air_exp_missing_salesManCode_returnsValidationError() throws Exception {
        mockMvc.perform(post("/api/house-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(airExpBody("HAWB-001", "AL001", null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/house-bl: AIR EXP — 12개 필수 모두 채움 → 201 + Location 헤더")
    void createHouseBl_air_exp_happyPath_returns201() throws Exception {
        Long id = 1L;
        given(houseBlAssembler.toCreateCommand(any(), any())).willReturn(mockCommand());
        given(houseBlUseCase.createHouseBl(any(CreateHouseBlCommand.class))).willReturn(id);

        mockMvc.perform(post("/api/house-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(airExpBody("HAWB-001", "AL001", "SALESMAN01")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/house-bl/1")))
                .andExpect(jsonPath("$.data.id").value(1));

        then(houseBlUseCase).should().createHouseBl(any(CreateHouseBlCommand.class));
    }

    // ── AIR IMP 필수 필드 누락 시나리오 ───────────────────────────────────

    @Test
    @DisplayName("POST /api/house-bl: AIR IMP — consigneeCode 누락 → 400")
    void createHouseBl_air_imp_missing_consigneeCode_returnsValidationError() throws Exception {
        mockMvc.perform(post("/api/house-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(airImpBody("HAWB-001", "AL001", "SALESMAN01", null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/house-bl: AIR IMP — 13개 필수(consigneeCode 포함) 모두 채움 → 201 + Location 헤더")
    void createHouseBl_air_imp_happyPath_returns201() throws Exception {
        Long id = 2L;
        given(houseBlAssembler.toCreateCommand(any(), any())).willReturn(mockCommand());
        given(houseBlUseCase.createHouseBl(any(CreateHouseBlCommand.class))).willReturn(id);

        mockMvc.perform(post("/api/house-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(airImpBody("HAWB-001", "AL001", "SALESMAN01", "CONSIGNEE01")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/house-bl/2")))
                .andExpect(jsonPath("$.data.id").value(2));

        then(houseBlUseCase).should().createHouseBl(any(CreateHouseBlCommand.class));
    }

    // ── 비필수 필드 누락 시나리오 ─────────────────────────────────────────

    @Test
    @DisplayName("POST /api/house-bl: AIR EXP — fhd(비필수) 누락이어도 검증 통과 → 201")
    void createHouseBl_air_exp_missing_fhd_returns201() throws Exception {
        Long id = 3L;
        given(houseBlAssembler.toCreateCommand(any(), any())).willReturn(mockCommand());
        given(houseBlUseCase.createHouseBl(any(CreateHouseBlCommand.class))).willReturn(id);

        // fhd 없이 airDetail 구성 (airlineCode만 포함)
        String body = airExpBodyWithoutFhd("HAWB-001", "AL001", "SALESMAN01");
        mockMvc.perform(post("/api/house-bl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        then(houseBlUseCase).should().createHouseBl(any(CreateHouseBlCommand.class));
    }

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────────────

    /**
     * AIR EXP 기본 필수 12개 필드 JSON 바디.
     * hblNo, airlineCode, salesManCode를 null로 전달해 누락 시나리오를 구성할 수 있다.
     */
    private String airExpBody(String hblNo, String airlineCode, String salesManCode) {
        String hblField = hblNo != null ? "\"hblNo\":\"" + hblNo + "\"," : "";
        String airlineField = airlineCode != null ? "\"airlineCode\":\"" + airlineCode + "\"" : "\"airlineCode\":null";
        String salesManField = salesManCode != null ? ",\"salesManCode\":\"" + salesManCode + "\"" : "";
        return "{"
                + "\"jobDiv\":\"AIR\","
                + "\"bound\":\"EXP\","
                + hblField
                + "\"shipmentType\":\"HOUSE\","
                + "\"freightTerm\":\"PREPAID\","
                + "\"polCode\":\"INIC\","
                + "\"podCode\":\"USLAX\","
                + "\"etd\":\"20260601\","
                + "\"eta\":\"20260610\","
                + "\"docPartnerCode\":\"DOC001\","
                + "\"operatorCode\":\"OP001\","
                + "\"teamCode\":\"TM001\","
                + "\"actualCustomerCode\":\"CUST001\""
                + salesManField
                + ",\"airDetail\":{"
                + airlineField
                + "}"
                + "}";
    }

    /**
     * airDetail에 fhd를 포함하지 않은 AIR EXP 바디 (fhd 비필수 시나리오 전용).
     */
    private String airExpBodyWithoutFhd(String hblNo, String airlineCode, String salesManCode) {
        return "{"
                + "\"jobDiv\":\"AIR\","
                + "\"bound\":\"EXP\","
                + "\"hblNo\":\"" + hblNo + "\","
                + "\"shipmentType\":\"HOUSE\","
                + "\"freightTerm\":\"PREPAID\","
                + "\"polCode\":\"INIC\","
                + "\"podCode\":\"USLAX\","
                + "\"etd\":\"20260601\","
                + "\"eta\":\"20260610\","
                + "\"docPartnerCode\":\"DOC001\","
                + "\"operatorCode\":\"OP001\","
                + "\"teamCode\":\"TM001\","
                + "\"actualCustomerCode\":\"CUST001\","
                + "\"salesManCode\":\"" + salesManCode + "\","
                + "\"airDetail\":{"
                + "\"airlineCode\":\"" + airlineCode + "\""
                + "}"
                + "}";
    }

    /**
     * AIR IMP 필수 13개 필드 JSON 바디 (consigneeCode 포함).
     * consigneeCode를 null로 전달해 누락 시나리오를 구성할 수 있다.
     */
    private String airImpBody(String hblNo, String airlineCode, String salesManCode, String consigneeCode) {
        String consigneeField = consigneeCode != null ? ",\"consigneeCode\":\"" + consigneeCode + "\"" : "";
        return "{"
                + "\"jobDiv\":\"AIR\","
                + "\"bound\":\"IMP\","
                + "\"hblNo\":\"" + hblNo + "\","
                + "\"shipmentType\":\"HOUSE\","
                + "\"freightTerm\":\"COLLECT\","
                + "\"polCode\":\"INIC\","
                + "\"podCode\":\"USLAX\","
                + "\"etd\":\"20260601\","
                + "\"eta\":\"20260610\","
                + "\"docPartnerCode\":\"DOC001\","
                + "\"operatorCode\":\"OP001\","
                + "\"teamCode\":\"TM001\","
                + "\"actualCustomerCode\":\"CUST001\","
                + "\"salesManCode\":\"" + salesManCode + "\""
                + consigneeField
                + ",\"airDetail\":{"
                + "\"airlineCode\":\"" + airlineCode + "\""
                + "}"
                + "}";
    }

    private CreateHouseBlCommand mockCommand() {
        return org.mockito.Mockito.mock(CreateHouseBlCommand.class);
    }
}
