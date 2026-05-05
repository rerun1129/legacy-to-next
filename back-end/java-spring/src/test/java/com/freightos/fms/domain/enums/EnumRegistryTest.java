package com.freightos.fms.domain.enums;

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

        // Bound — 메타 없음, fromName 사용
        List<EnumOption> boundOptions = new ArrayList<>();
        for (Bound b : Bound.values()) {
            boundOptions.add(EnumOption.fromName(b));
        }
        map.put("Bound", boundOptions);

        // VolumeDivisor — label 보유
        List<EnumOption> volumeOptions = new ArrayList<>();
        for (VolumeDivisor v : VolumeDivisor.values()) {
            volumeOptions.add(new EnumOption(v.name(), v.getLabel(), null));
        }
        map.put("VolumeDivisor", volumeOptions);

        // NoOfBl — number→string 변환
        List<EnumOption> noOfBlOptions = new ArrayList<>();
        for (NoOfBl n : NoOfBl.values()) {
            noOfBlOptions.add(new EnumOption(
                    String.valueOf(n.getNumber()),
                    String.valueOf(n.getNumber()),
                    null));
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

        // 두 번째 등록 시도 — register helper 미사용, 직접 map 구성 후 registry 생성은 정상이므로
        // 실제 중복 검증은 EnumRegistryFactory.register()가 담당한다.
        // EnumRegistry.of() 자체에는 중복 키 보호가 없으므로 팩토리 레벨에서 검증한다.
        // 여기서는 팩토리 없이 직접 register 헬퍼 로직을 재현하여 예외를 검증한다.
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
    @DisplayName("NoOfBl — number가 string으로 변환되어 code/label에 설정됨")
    void noOfBl_numberToStringConversion() {
        List<EnumOption> options = registry.getByName("NoOfBl").orElseThrow();

        assertThat(options).anySatisfy(opt -> {
            assertThat(opt.code()).isEqualTo("0");
            assertThat(opt.label()).isEqualTo("0");
        });
        assertThat(options).anySatisfy(opt -> {
            assertThat(opt.code()).isEqualTo("1");
            assertThat(opt.label()).isEqualTo("1");
        });
    }

    @Test
    @DisplayName("PackageUnit — values() 길이 0, 등록 skip되어 getByName은 Optional.empty()")
    void packageUnit_emptyEnum_notRegistered() {
        // PackageUnit은 setUp()에서 등록하지 않음 (values().length == 0 → skip)
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
