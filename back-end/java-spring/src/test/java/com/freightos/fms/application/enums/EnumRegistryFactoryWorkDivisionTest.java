package com.freightos.fms.application.enums;

import com.freightos.fms.application.enums.projection.EnumOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EnumRegistryFactory — WorkDivision 등록 검증")
class EnumRegistryFactoryWorkDivisionTest {

    private EnumRegistry registry;

    @BeforeEach
    void setUp() {
        EnumRegistryFactory factory = new EnumRegistryFactory();
        registry = factory.enumRegistry();
    }

    @Test
    @DisplayName("getByName(\"WorkDivision\") 은 4개 옵션을 반환한다")
    void workDivision_registeredWithFourOptions() {
        Optional<List<EnumOption>> result = registry.getByName("WorkDivision");

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(4);
    }

    @Test
    @DisplayName("WorkDivision 옵션에 SEA, AIR, WAREHOUSE, TRUCKING 이 모두 포함된다")
    void workDivision_containsAllFourCodes() {
        List<EnumOption> options = registry.getByName("WorkDivision").orElseThrow();

        assertThat(options).extracting(EnumOption::code)
                .containsExactly("SEA", "AIR", "WAREHOUSE", "TRUCKING");
    }

    @Test
    @DisplayName("WorkDivision 각 옵션의 label이 올바르게 매핑된다")
    void workDivision_labelsAreCorrect() {
        List<EnumOption> options = registry.getByName("WorkDivision").orElseThrow();

        assertThat(options).anySatisfy(opt -> {
            assertThat(opt.code()).isEqualTo("SEA");
            assertThat(opt.label()).isEqualTo("Sea");
        });
        assertThat(options).anySatisfy(opt -> {
            assertThat(opt.code()).isEqualTo("AIR");
            assertThat(opt.label()).isEqualTo("Air");
        });
        assertThat(options).anySatisfy(opt -> {
            assertThat(opt.code()).isEqualTo("WAREHOUSE");
            assertThat(opt.label()).isEqualTo("Warehouse");
        });
        assertThat(options).anySatisfy(opt -> {
            assertThat(opt.code()).isEqualTo("TRUCKING");
            assertThat(opt.label()).isEqualTo("Trucking");
        });
    }

    @Test
    @DisplayName("WorkDivision 옵션의 description은 null이다")
    void workDivision_descriptionIsNull() {
        List<EnumOption> options = registry.getByName("WorkDivision").orElseThrow();

        assertThat(options).allSatisfy(opt -> assertThat(opt.description()).isNull());
    }
}
