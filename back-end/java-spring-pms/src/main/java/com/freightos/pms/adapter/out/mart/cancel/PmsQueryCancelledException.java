package com.freightos.pms.adapter.out.mart.cancel;

/**
 * 새 조회 요청이 도착해 진행 중인 정확 count 연산을 능동 취소했을 때 발생하는 예외.
 *
 * line-accel=true 환경에서 PmsExactCountRegistry.onNewSearch → killByComment → Mongo 강제 중단 시
 * catch(RuntimeException) 분기에서 op.killed 플래그로 식별 후 throw된다.
 * FE는 이미 HTTP abort를 완료한 상태이므로 이 응답은 실질적으로 폐기된다.
 * GlobalExceptionHandler는 409 + debug 로그만 출력하여 전역 에러 토스트로 새지 않게 처리한다.
 */
public class PmsQueryCancelledException extends RuntimeException {

    public PmsQueryCancelledException() {
        super("exact count 연산이 신규 조회에 의해 취소됐습니다.");
    }
}
