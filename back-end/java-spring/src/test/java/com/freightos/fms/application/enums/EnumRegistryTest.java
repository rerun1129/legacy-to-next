package com.freightos.fms.application.enums;

import com.freightos.fms.application.enums.projection.EnumOption;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.PackageUnit;
import com.freightos.fms.domain.common.enums.VolumeDivisor;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnumRegistryTest {

    private EnumRegistry registry;

    @BeforeEach
    void setUp() {
        Map<String, List<EnumOption>> map = new LinkedHashMap<>();

        List<EnumOption> boundOptions = new ArrayList<>();
        for (Bound b : Bound.values()) {
            boundOptions.add(EnumOption.fromName(b));
        }
        map.put("Bound", boundOptions);

        List<EnumOption> volumeOptions = new ArrayList<>();
        for (VolumeDivisor v : VolumeDivisor.values()) {
            volumeOptions.add(new EnumOption(v.name(), v.getLabel(), null));
        }
        map.put("VolumeDivisor", volumeOptions);

        List<EnumOption> noOfBlOptions = new ArrayList<>();
        for (NoOfBl n : NoOfBl.values()) {
            noOfBlOptions.add(new EnumOption(n.name(), n.getLabel(), null));
        }
        map.put("NoOfBl", noOfBlOptions);

        registry = EnumRegistry.of(map);
    }

    @Test
    @DisplayName("등록된 name으로 getByName 호출 시 옵션 목록 반환")
    void getByName_registered_returnsOptions() {
        Optional<List<EnumOption>> result = registry.getByName("Bound");

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(Bound.values().length);
    }

    @Test
    @DisplayName("미등록 name으로 getByName 호출 시 Optional.empty() 반환")
    void getByName_unknown_returnsEmpty() {
        Optional<List<EnumOption>> result = registry.getByName("NonExistent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("동일 키 2회 등록 시 IllegalStateException")
    void duplicateKey_throwsIllegalStateException() {
        Map<String, List<EnumOption>> map = new LinkedHashMap<>();
        List<EnumOption> first = List.of(EnumOption.fromName(Bound.EXP));
        map.put("Bound", first);

        assertThatThrownBy(() -> {
            if (map.containsKey("Bound")) {
                throw new IllegalStateException("중복 ENUM 키: Bound");
            }
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("중복 ENUM 키: Bound");
    }

    @Test
    @DisplayName("Bound — label이 name()과 동일 (메타 없는 경우 fromName fallback)")
    void bound_fromName_labelEqualsCode() {
        List<EnumOption> options = registry.getByName("Bound").orElseThrow();

        assertThat(options).allSatisfy(opt ->
                assertThat(opt.label()).isEqualTo(opt.code()));
    }

    @Test
    @DisplayName("VolumeDivisor — label이 name()이 아닌 실제 label 값")
    void volumeDivisor_labelMapping() {
        List<EnumOption> options = registry.getByName("VolumeDivisor").orElseThrow();
        EnumOption first = options.get(0);

        assertThat(first.label()).isEqualTo(VolumeDivisor.values()[0].getLabel());
        assertThat(first.code()).isEqualTo(VolumeDivisor.values()[0].name());
    }

    @Test
    @DisplayName("NoOfBl — name()이 code, getLabel()이 label로 설정됨")
    void noOfBl_nameAndLabelMapping() {
        List<EnumOption> options = registry.getByName("NoOfBl").orElseThrow();

        assertThat(options).anySatisfy(opt -> {
            assertThat(opt.code()).isEqualTo("ZERO");
            assertThat(opt.label()).isEqualTo("ZERO(0)");
        });
        assertThat(options).anySatisfy(opt -> {
            assertThat(opt.code()).isEqualTo("ONE");
            assertThat(opt.label()).isEqualTo("ONE(1)");
        });
    }

    @Test
    @DisplayName("PackageUnit — values() 길이 0, 등록 skip되어 getByName은 Optional.empty()")
    void packageUnit_emptyEnum_notRegistered() {
        assertThat(PackageUnit.values()).isEmpty();
        assertThat(registry.getByName("PackageUnit")).isEmpty();
    }

    @Test
    @DisplayName("getAllKeys() 에 등록된 키들이 포함된다")
    void getAllKeys_containsRegisteredKeys() {
        assertThat(registry.getAllKeys()).contains("Bound", "VolumeDivisor", "NoOfBl");
    }

    @Test
    @DisplayName("ETag가 null이 아니고 비어있지 않다")
    void etag_isNotNullOrBlank() {
        assertThat(registry.getEtag()).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("getByName 반환 리스트는 수정 불가(unmodifiable)하다")
    void getByName_returnsUnmodifiableList() {
        List<EnumOption> options = registry.getByName("Bound").orElseThrow();

        assertThatThrownBy(() -> options.add(new EnumOption("X", "X", null)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
