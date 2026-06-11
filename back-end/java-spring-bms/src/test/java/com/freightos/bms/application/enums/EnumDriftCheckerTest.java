package com.freightos.bms.application.enums;

import com.freightos.bms.application.enums.port.out.CommonCodeReadPort;
import com.freightos.bms.application.enums.projection.EnumOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnumDriftCheckerTest {

    @Mock
    private CommonCodeReadPort dbPort;

    @Test
    @DisplayName("DB 그룹 없음 — 드리프트 체크 스킵(빈 Optional 수신)")
    void checkDrift_dbGroupAbsent_skipsSilently() {
        EnumRegistry registry = EnumRegistry.of(Map.of(
                "DocumentType", List.of(new EnumOption("INVOICE", "Invoice", null))));
        given(dbPort.findByGroupCode("DocumentType")).willReturn(Optional.empty());

        EnumDriftChecker checker = new EnumDriftChecker(registry, dbPort);
        checker.checkDrift();

        verify(dbPort).findByGroupCode("DocumentType");
    }

    @Test
    @DisplayName("DB 조회 예외 — 앱 계속 기동(예외 전파 없음)")
    void checkDrift_dbException_doesNotPropagate() {
        EnumRegistry registry = EnumRegistry.of(Map.of(
                "DocumentType", List.of(new EnumOption("INVOICE", "Invoice", null))));
        given(dbPort.findByGroupCode("DocumentType")).willThrow(new RuntimeException("DB down"));

        EnumDriftChecker checker = new EnumDriftChecker(registry, dbPort);
        checker.checkDrift();
        // 예외가 전파되지 않으면 통과
    }
}
