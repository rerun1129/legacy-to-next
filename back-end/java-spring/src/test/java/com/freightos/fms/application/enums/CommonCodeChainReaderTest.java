package com.freightos.fms.application.enums;

import com.freightos.fms.application.enums.port.out.CommonCodeCachePort;
import com.freightos.fms.application.enums.port.out.CommonCodeReadPort;
import com.freightos.fms.application.enums.projection.EnumOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class CommonCodeChainReaderTest {

    @Mock
    private CommonCodeCachePort cachePort;

    @Mock
    private CommonCodeReadPort dbPort;

    private CommonCodeChainReader sut;

    @BeforeEach
    void setUp() {
        sut = new CommonCodeChainReader(cachePort, dbPort);
    }

    @Test
    @DisplayName("Redis hit — DB 미조회, 캐시 값 반환")
    void resolve_redisHit_returnsCachedValue() {
        List<EnumOption> cached = List.of(new EnumOption("A", "Alpha", null));
        given(cachePort.get("Bound")).willReturn(Optional.of(cached));

        Optional<List<EnumOption>> result = sut.resolve("Bound");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(cached);
        then(dbPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Redis miss → DB hit — DB 결과 Redis에 저장 후 반환")
    void resolve_redisMissDbHit_putsInCacheAndReturns() {
        List<EnumOption> dbOptions = List.of(new EnumOption("I", "Import", null, "수입"));
        given(cachePort.get("Bound")).willReturn(Optional.empty());
        given(dbPort.findByGroupCode("Bound")).willReturn(Optional.of(dbOptions));

        Optional<List<EnumOption>> result = sut.resolve("Bound");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(dbOptions);
        then(cachePort).should().put(eq("Bound"), eq(dbOptions));
    }

    @Test
    @DisplayName("Redis miss → DB miss — 빈 Optional 반환")
    void resolve_redisMissDbMiss_returnsEmpty() {
        given(cachePort.get("Unknown")).willReturn(Optional.empty());
        given(dbPort.findByGroupCode("Unknown")).willReturn(Optional.empty());

        Optional<List<EnumOption>> result = sut.resolve("Unknown");

        assertThat(result).isEmpty();
        then(cachePort).should().get("Unknown");
        then(cachePort).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("Redis 예외 — ChainReader가 예외를 전파한다(어댑터에서 삼키는 계약은 별도 테스트)")
    void resolve_redisException_propagatesException() {
        RuntimeException redisError = new RuntimeException("Redis connection failed");
        given(cachePort.get("Bound")).willThrow(redisError);

        // ChainReader는 cachePort 예외를 삼키지 않으므로 그대로 전파된다
        assertThatThrownBy(() -> sut.resolve("Bound"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Redis connection failed");

        then(dbPort).shouldHaveNoInteractions();
    }
}
