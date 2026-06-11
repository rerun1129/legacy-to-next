package com.freightos.fms.application.enums;

import com.freightos.fms.application.enums.port.out.CommonCodeReadPort;
import com.freightos.fms.application.enums.projection.EnumOption;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class EnumDriftCheckerTest {

    @Mock
    private CommonCodeReadPort dbPort;

    @Test
    @DisplayName("Java·DB 집합 일치 — WARN 없음(정상 케이스)")
    void checkDrift_noMismatch_noWarnLogged() {
        EnumRegistry registry = EnumRegistry.of(Map.of(
                "Bound", List.of(new EnumOption("I", "Import", null), new EnumOption("E", "Export", null))));
        given(dbPort.findByGroupCode("Bound")).willReturn(Optional.of(
                List.of(new EnumOption("I", "수입", null), new EnumOption("E", "수출", null))));

        EnumDriftChecker checker = new EnumDriftChecker(registry, dbPort);
        checker.checkDrift();

        verify(dbPort).findByGroupCode("Bound");
        verifyNoMoreInteractions(dbPort);
        // 로그 WARN이 없음을 검증하려면 LogCaptor/Appender가 필요하므로 호출 수 검증만 수행
    }

    @Test
    @DisplayName("DB 그룹 없음 — 드리프트 체크 스킵(빈 Optional 수신)")
    void checkDrift_dbGroupAbsent_skipsSilently() {
        EnumRegistry registry = EnumRegistry.of(Map.of(
                "Bound", List.of(new EnumOption("I", "Import", null))));
        given(dbPort.findByGroupCode("Bound")).willReturn(Optional.empty());

        EnumDriftChecker checker = new EnumDriftChecker(registry, dbPort);
        checker.checkDrift();

        verify(dbPort).findByGroupCode("Bound");
    }

    @Test
    @DisplayName("DB 조회 예외 — 앱 계속 기동(예외 전파 없음)")
    void checkDrift_dbException_doesNotPropagate() {
        EnumRegistry registry = EnumRegistry.of(Map.of(
                "Bound", List.of(new EnumOption("I", "Import", null))));
        given(dbPort.findByGroupCode("Bound")).willThrow(new RuntimeException("DB down"));

        EnumDriftChecker checker = new EnumDriftChecker(registry, dbPort);
        // 예외가 전파되지 않아야 한다
        checker.checkDrift();
    }
}
