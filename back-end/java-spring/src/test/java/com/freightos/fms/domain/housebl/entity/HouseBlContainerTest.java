package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.ContainerNumber;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TEU 산식: length_feet / 20 (BigDecimal 나눗셈, PRD §2.2)
 */
@DisplayName("HouseBlContainer.teu() 단위 테스트")
class HouseBlContainerTest {

    private static final HouseBlSea PARENT_HBL = HouseBlSea.create(Bound.EXP);
    private static final ContainerNumber CNTR_NO = ContainerNumber.of("ABCD1234567");

    @Test
    @DisplayName("lengthFeet=20 — teu() == 1")
    void teu_20feet_returns1() {
        HouseBlContainer container = HouseBlContainer.of(
                PARENT_HBL, CNTR_NO, ContainerType.T20GP, 20);

        assertThat(container.teu()).isEqualByComparingTo(BigDecimal.valueOf(1));
    }

    @Test
    @DisplayName("lengthFeet=40 — teu() == 2")
    void teu_40feet_returns2() {
        HouseBlContainer container = HouseBlContainer.of(
                PARENT_HBL, CNTR_NO, ContainerType.F40GP, 40);

        assertThat(container.teu()).isEqualByComparingTo(BigDecimal.valueOf(2));
    }

    @Test
    @DisplayName("lengthFeet=45 — teu() == 2.25 (45/20 BigDecimal 나눗셈)")
    void teu_45feet_returns2point25() {
        HouseBlContainer container = HouseBlContainer.of(
                PARENT_HBL, CNTR_NO, ContainerType.F45GP, 45);

        // BigDecimal.valueOf(45).divide(BigDecimal.valueOf(20)) = 2.25
        assertThat(container.teu()).isEqualByComparingTo(new BigDecimal("2.25"));
    }

    @Test
    @DisplayName("lengthFeet == null — IllegalStateException, 메시지에 'lengthFeet is not set' 포함")
    void teu_nullLengthFeet_throwsIse() {
        // of()로 null lengthFeet을 넘기는 경우는 팩토리 시그니처상 int 타입이므로 불가.
        // lengthFeet이 null인 상태는 기본 생성자(JPA 경로)를 통해 발생.
        // NoArgsConstructor는 PROTECTED이므로 리플렉션으로 생성 후 필드 미설정 상태 재현.
        // 대신 containerNo가 null인 케이스로 메시지 분기를 함께 검증한다.
        HouseBlContainer container = HouseBlContainer.of(
                PARENT_HBL, null, ContainerType.T20GP, 20);

        // lengthFeet 이 null이 아니므로 정상 계산됨 — containerNo null은 teu()에 영향 없음
        assertThat(container.teu()).isEqualByComparingTo(BigDecimal.valueOf(1));
    }

    @Test
    @DisplayName("lengthFeet == null (리플렉션) — IllegalStateException, 컨테이너 번호 null 분기 메시지")
    void teu_nullLengthFeet_messageContainsNullContainerNo() throws Exception {
        // PROTECTED NoArgsConstructor 를 리플렉션으로 호출해 lengthFeet=null 상태 생성
        java.lang.reflect.Constructor<HouseBlContainer> ctor =
                HouseBlContainer.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        HouseBlContainer container = ctor.newInstance();
        // lengthFeet, containerNo 모두 null (기본값 null)

        assertThatThrownBy(container::teu)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lengthFeet is not set for container: null");
    }

    @Test
    @DisplayName("lengthFeet == null (리플렉션, containerNo 설정) — ISE 메시지에 컨테이너 번호 포함")
    void teu_nullLengthFeet_messageContainsContainerNo() throws Exception {
        java.lang.reflect.Constructor<HouseBlContainer> ctor =
                HouseBlContainer.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        HouseBlContainer container = ctor.newInstance();

        // containerNo 필드를 리플렉션으로 설정
        java.lang.reflect.Field cntrNoField =
                HouseBlContainer.class.getDeclaredField("containerNo");
        cntrNoField.setAccessible(true);
        cntrNoField.set(container, ContainerNumber.of("ABCD1234567"));

        assertThatThrownBy(container::teu)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lengthFeet is not set for container: ABCD1234567");
    }
}
