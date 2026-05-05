package com.freightos.fms.application.enums;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.enums.EnumOption;
import com.freightos.fms.domain.enums.EnumRegistry;
import com.freightos.fms.domain.enums.port.in.EnumQueryResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private EnumRegistry enumRegistry;

    @InjectMocks
    private EnumQueryService enumQueryService;

    @Test
    @DisplayName("getByName — registry가 Optional.of(List) 반환 시 정상 반환")
    void getByName_registryReturnsOptions_returnsOptions() {
        List<EnumOption> options = List.of(
                new EnumOption("EXP", "EXP", null),
                new EnumOption("IMP", "IMP", null));
        given(enumRegistry.getByName("Bound")).willReturn(Optional.of(options));

        List<EnumOption> result = enumQueryService.getByName("Bound");

        assertThat(result).isEqualTo(options);
        then(enumRegistry).should().getByName("Bound");
    }

    @Test
    @DisplayName("getByName — registry가 Optional.empty() 반환 시 ResourceNotFoundException throw")
    void getByName_registryReturnsEmpty_throwsResourceNotFoundException() {
        given(enumRegistry.getByName("Unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> enumQueryService.getByName("Unknown"))
                .isInstanceOf(ResourceNotFoundException.class);

        then(enumRegistry).should().getByName("Unknown");
    }

    @Test
    @DisplayName("getByNames — 존재하는 name은 found에, 미존재 name은 notFound에 분류된다")
    void getByNames_mixedNames_classifiesFoundAndNotFound() {
        List<EnumOption> boundOptions = List.of(new EnumOption("EXP", "EXP", null));
        List<EnumOption> perOptions = List.of(new EnumOption("SHP", "Ship", "Ship"));
        given(enumRegistry.getByName("Bound")).willReturn(Optional.of(boundOptions));
        given(enumRegistry.getByName("Per")).willReturn(Optional.of(perOptions));
        given(enumRegistry.getByName("Unknown")).willReturn(Optional.empty());

        EnumQueryResult result = enumQueryService.getByNames(List.of("Bound", "Per", "Unknown"));

        assertThat(result.found()).containsKeys("Bound", "Per");
        assertThat(result.found()).doesNotContainKey("Unknown");
        assertThat(result.notFound()).containsExactly("Unknown");
    }

    @Test
    @DisplayName("getByNames — 모두 미존재 시 found는 비어있고 notFound에 모두 담긴다")
    void getByNames_allUnknown_foundEmptyNotFoundFull() {
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
        given(enumRegistry.getByName("Bound")).willReturn(Optional.of(options));
        given(enumRegistry.getByName("BlType")).willReturn(Optional.of(options));

        EnumQueryResult result = enumQueryService.getByNames(List.of("Bound", "BlType"));

        assertThat(result.found()).hasSize(2);
        assertThat(result.notFound()).isEmpty();
    }
}
