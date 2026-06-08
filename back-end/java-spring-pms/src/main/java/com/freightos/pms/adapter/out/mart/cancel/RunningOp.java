package com.freightos.pms.adapter.out.mart.cancel;

/**
 * 진행 중인 exact count 연산 컨텍스트.
 *
 * comment: Mongo aggregation에 주입된 op 식별 태그. killByComment가 이 값으로 currentOp를 조회한다.
 * signature: 필터 서명. onNewSearch에서 이전 op의 서명과 다를 때만 취소한다(페이지 이동 보호).
 * killed: killByComment 호출 직전에 true로 설정되어, catch 분기에서 "benign 취소"임을 식별한다.
 */
public final class RunningOp {

    final String comment;
    final String signature;
    volatile boolean killed;

    RunningOp(String comment, String signature) {
        this.comment = comment;
        this.signature = signature;
        this.killed = false;
    }

    public String getComment() {
        return comment;
    }

    public boolean isKilled() {
        return killed;
    }
}
