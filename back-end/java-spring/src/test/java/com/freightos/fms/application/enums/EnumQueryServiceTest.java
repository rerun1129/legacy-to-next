package com.freightos.fms.application.enums;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.enums.projection.EnumOption;
import com.freightos.fms.application.enums.port.in.EnumQueryResult;
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

@ExtendWith(MockitoExtension.class)
class EnumQueryServiceTest {

    @Mock
    private CommonCodeChainReader chainReader;

    @Mock
    private EnumRegistry enumRegistry;

    private EnumQueryService enumQueryService;

    @BeforeEach
    void setUp() {
        enumQueryService = new EnumQueryService(chainReader, enumRegistry);
    }

    @Test
    @DisplayName("getByName — chainReader가 hit 시 registry 미조회, 체인 결과 반환")
    void getByName_chainHit_returnsChainResult() {
        List<EnumOption> options = List.of(new EnumOption("EXP", "EXP", null));
        given(chainReader.resolve("Bound")).willReturn(Optional.of(options));

        List<EnumOption> result = enumQueryService.getByName("Bound");

        assertThat(result).isEqualTo(options);
        then(enumRegistry).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("getByName — chainReader miss, registry hit — registry 결과 반환")
    void getByName_chainMissRegistryHit_returnsRegistryResult() {
        List<EnumOption> options = List.of(new EnumOption("EXP", "EXP", null));
        given(chainReader.resolve("Bound")).willReturn(Optional.empty());
        given(enumRegistry.getByName("Bound")).willReturn(Optional.of(options));

        List<EnumOption> result = enumQueryService.getByName("Bound");

        assertThat(result).isEqualTo(options);
        then(enumRegistry).should().getByName("Bound");
    }

    @Test
    @DisplayName("getByName — 체인·registry 모두 miss → ResourceNotFoundException throw")
    void getByName_allMiss_throwsResourceNotFoundException() {
        given(chainReader.resolve("Unknown")).willReturn(Optional.empty());
        given(enumRegistry.getByName("Unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> enumQueryService.getByName("Unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getByNames — 존재하는 name은 found에, 미존재 name은 notFound에 분류된다")
    void getByNames_mixedNames_classifiesFoundAndNotFound() {
        List<EnumOption> boundOptions = List.of(new EnumOption("EXP", "EXP", null));
        given(chainReader.resolve("Bound")).willReturn(Optional.of(boundOptions));
        given(chainReader.resolve("Unknown")).willReturn(Optional.empty());
        given(enumRegistry.getByName("Unknown")).willReturn(Optional.empty());

        EnumQueryResult result = enumQueryService.getByNames(List.of("Bound", "Unknown"));

        assertThat(result.found()).containsKey("Bound");
        assertThat(result.notFound()).containsExactly("Unknown");
    }

    @Test
    @DisplayName("getByNames — 모두 미존재 시 found 비어있고 notFound에 모두 담긴다")
    void getByNames_allUnknown_foundEmptyNotFoundFull() {
        given(chainReader.resolve("X")).willReturn(Optional.empty());
        given(chainReader.resolve("Y")).willReturn(Optional.empty());
        given(enumRegistry.getByName("X")).willReturn(Optional.empty());
        given(enumRegistry.getByName("Y")).willReturn(Optional.empty());

        EnumQueryResult result = enumQueryService.getByNames(List.of("X", "Y"));

        assertThat(result.found()).isEmpty();
        assertThat(result.notFound()).containsExactly("X", "Y");
    }

    @Test
    @DisplayName("getByNames — 모두 존재 시 notFound는 비어있다")
    void getByNames_allFound_notFoundEmpty() {
        List<EnumOption> options = List.of(new EnumOption("A", "A", null));
        given(chainReader.resolve("Bound")).willReturn(Optional.of(options));
        given(chainReader.resolve("BlType")).willReturn(Optional.of(options));

        EnumQueryResult result = enumQueryService.getByNames(List.of("Bound", "BlType"));

        assertThat(result.found()).hasSize(2);
        assertThat(result.notFound()).isEmpty();
    }
}
