# Deferrable 위반 누적 처리

> Reviewer 사이클에서 `blocking: false` 위반이 발생할 때 참조하는 흐름. 일반 사이클에는 영향 없음.

## 정의

외부 Reviewer가 violations 항목에 `blocking: false`로 분류한 위반. 기본적으로 `rules/conventions.md`(CONV1~CONV7)만 해당. 데이터 손실·보안 위험이 없는 순수 코드 컨벤션 위반.

blocking=true 기본 대상: `architecture.md`(ARCH1~ARCH6), `correctness.md`(C1~C8), `ai_specific.md`(A1~A12).

## 누적 저장소

- 위치: `.claude/deferred_review.json` (**git 추적 대상** — PC 간 동기화)
- 단위: Reviewer 호출 1회 = `cycles[]` 항목 1개 (위반 개수 무관)
- 임계: `REVIEWER_DEFERRED_THRESHOLD` 환경변수 (기본값: 5 사이클)

## Snooze

임계 도달 시 사용자가 "계속 누적"을 선택하면 `.claude/.deferred_snooze`에 오프셋(+5)을 기록. 다음 flush 시 자동 삭제.

## 메인 처리 절차 (`[ESCALATE_TO_USER:DEFERRED_BATCH]` 수신 시)

QA 완료 후(성공/실패 무관):
1. `.claude/deferred_review.json` Read → 사이클별 위반을 markdown으로 정리해 사용자에게 출력
2. 3가지 옵션 제시:
   - "지금 일괄 처리" → `/pipeline-deferred-flush` 호출
   - "계속 누적" → `.claude/.deferred_snooze`에 현재 오프셋 +5 기록
   - "취소" → 그대로 종료
3. 사용자 응답대로 실행

## 카운터 정책

- `.reject_count`는 blocking REJECTED만 카운트
- deferrable-only REJECTED는 카운터 리셋 (gate 통과로 간주)
- 결과: `blocking → deferrable-only → blocking` 시퀀스는 [ESCALATE_TO_USER] 미발동 (의도적)

## 누적 모드와의 관계

`/pipeline-start`는 Reviewer를 발동시키지 않으므로 deferrable 누적도 발생하지 않는다. 누적은 `/pipeline-review` 또는 `/pipeline` 사이클에서만 일어난다.
