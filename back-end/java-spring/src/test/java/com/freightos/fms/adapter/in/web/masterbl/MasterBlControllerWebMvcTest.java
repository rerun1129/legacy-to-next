package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.domain.masterbl.MasterBlDetail;
import com.freightos.fms.domain.masterbl.port.in.MasterBlUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MasterBlController.class)
@ActiveProfiles("test")
class MasterBlControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MasterBlUseCase masterBlUseCase;

    @MockBean
    private MasterBlAssembler masterBlAssembler;

    // @EnableJpaAuditing이 FmsApplication에 선언되어 WebMvcTest 슬라이스에서도 JpaMetamodelMappingContext를 요구함
    @MockBean
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
}
