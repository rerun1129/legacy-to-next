package com.freightos.pms.adapter.out.mart.cancel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 사용자별 진행 중인 exact count 연산을 추적하고,
 * 새 조회가 도착하면 이전 연산을 즉시 취소한다.
 *
 * 서명(signature)이 같으면 페이지 이동으로 간주해 취소하지 않는다.
 * 서명이 다르면 필터 변경 재조회이므로 이전 op을 죽인다.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "pms.mart.line-accel", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsExactCountRegistry {

    private final PmsMongoOpKiller killer;
    private final ConcurrentHashMap<String, RunningOp> running = new ConcurrentHashMap<>();

    /**
     * 새 조회 도착 시 호출한다.
     * 직전 연산이 다른 서명이면 즉시 취소한다(페이지 이동 동일 서명은 보호).
     *
     * @param userKey   사용자 식별 키
     * @param signature 필터 서명({@link com.freightos.pms.adapter.out.mart.PmsPerformanceFilterSignature})
     */
    public void onNewSearch(String userKey, String signature) {
        RunningOp existing = running.get(userKey);
        if (existing == null || existing.signature.equals(signature)) return;

        // 다른 필터의 정확 count — killed 플래그 먼저 set 후 killOp
        existing.killed = true;
        killer.killByComment(existing.comment);
        running.remove(userKey, existing);
        log.debug("onNewSearch: cancelled previous exact count user={} prevSig={} newSig={}",
                userKey, existing.signature, signature);
    }

    /**
     * exact count 연산 시작 시 호출한다.
     * 반환된 {@link RunningOp}의 comment를 aggregation/query에 주입하고,
     * op.killed를 catch 분기에서 참조한다.
     *
     * @param userKey   사용자 식별 키
     * @param signature 필터 서명
     * @return 생성된 RunningOp(comment·signature·killed 포함)
     */
    public RunningOp begin(String userKey, String signature) {
        String comment = "pms-exact-" + UUID.randomUUID();
        RunningOp op = new RunningOp(comment, signature);
        running.put(userKey, op);
        log.debug("begin: started exact count user={} comment={}", userKey, comment);
        return op;
    }

    /**
     * exact count 연산 완료(정상·예외 무관) 시 호출한다.
     * 현재 등록분과 동일 op일 때만 제거한다(신규 op이 이미 교체한 경우 제거 금지).
     *
     * @param userKey 사용자 식별 키
     * @param op      begin이 반환한 RunningOp
     */
    public void complete(String userKey, RunningOp op) {
        running.remove(userKey, op);
        log.debug("complete: finished exact count user={} comment={}", userKey, op.comment);
    }
}
