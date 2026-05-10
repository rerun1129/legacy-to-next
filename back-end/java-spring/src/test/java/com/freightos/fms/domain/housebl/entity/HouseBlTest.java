package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.PortCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * hbl_no update 차단 회귀 보호 테스트.
 * HouseBlUpdateFields에 hblNo 필드가 없으므로 컴파일 수준에서 이미 보장되나,
 * 정책 명문화 및 향후 리그레션 방지를 위해 동작 수준 검증을 추가한다.
 */
@DisplayName("HouseBl.update() — hblNo 불변 보장")
class HouseBlTest {

    // HouseBl은 abstract이므로 구체 구현인 HouseBlSea를 사용한다.
    private static HouseBlSea seaWithHblNo(String hblNo) {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        sea.assignHblNo(BlNumber.of(hblNo));
        return sea;
    }

    private static HouseBl.HouseBlUpdateFields allNullFields() {
        // 모든 필드 null → PATCH 의미론 상 기존 값 유지
        return new HouseBl.HouseBlUpdateFields(
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null
        );
    }

    @Test
    @DisplayName("update() 호출 후 assignHblNo()로 설정한 hblNo가 그대로 유지된다")
    void update_doesNotChangeHblNo() {
        String original = "ORIG-BL-001";
        HouseBlSea sea = seaWithHblNo(original);

        sea.update(allNullFields());

        assertThat(sea.getHblNo()).isNotNull();
        assertThat(sea.getHblNo().value()).isEqualTo(original);
    }

    @Test
    @DisplayName("update()에 다른 필드(polCode)가 있어도 hblNo는 변경되지 않는다")
    void update_withOtherFields_doesNotChangeHblNo() {
        String original = "ORIG-BL-002";
        HouseBlSea sea = seaWithHblNo(original);

        // hblNo 없이 polCode만 포함된 UpdateFields
        HouseBl.HouseBlUpdateFields fields = new HouseBl.HouseBlUpdateFields(
                null, null, null, null, null, null,
                PortCode.of("KRPUS"), null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null
        );

        sea.update(fields);

        assertThat(sea.getHblNo()).isNotNull();
        assertThat(sea.getHblNo().value()).isEqualTo(original);
    }
}
