package com.freightos.bms.application.enums;

import com.freightos.bms.application.enums.port.out.CommonCodeCachePort;
import com.freightos.bms.application.enums.port.out.CommonCodeReadPort;
import com.freightos.bms.application.enums.projection.EnumOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
        List<EnumOption> cached = List.of(new EnumOption("INVOICE", "Invoice", null));
        given(cachePort.get("DocumentType")).willReturn(Optional.of(cached));

        Optional<List<EnumOption>> result = sut.resolve("DocumentType");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(cached);
        then(dbPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Redis miss → DB hit — DB 결과 Redis에 저장 후 반환")
    void resolve_redisMissDbHit_putsInCacheAndReturns() {
        List<EnumOption> dbOptions = List.of(new EnumOption("CREATED", "Created", null));
        given(cachePort.get("DocumentStatus")).willReturn(Optional.empty());
        given(dbPort.findByGroupCode("DocumentStatus")).willReturn(Optional.of(dbOptions));

        Optional<List<EnumOption>> result = sut.resolve("DocumentStatus");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(dbOptions);
        then(cachePort).should().put(eq("DocumentStatus"), eq(dbOptions));
    }

    @Test
    @DisplayName("Redis miss → DB miss — 빈 Optional 반환")
    void resolve_redisMissDbMiss_returnsEmpty() {
        given(cachePort.get("Unknown")).willReturn(Optional.empty());
        given(dbPort.findByGroupCode("Unknown")).willReturn(Optional.empty());

        Optional<List<EnumOption>> result = sut.resolve("Unknown");

        assertThat(result).isEmpty();
    }
}
