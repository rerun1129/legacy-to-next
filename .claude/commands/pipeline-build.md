---
description: 작업 지시를 큐(.claude/.task_queue)에 적재만 한다. 실행은 `/pipeline-start`로 트리거.
allowed-tools: Bash, Read, Write
---

작업 지시를 `.claude/.task_queue`에 추가하고 종료한다. 파이프라인은 실행하지 않는다.

## 진입 처리

리뷰 스킵 sentinel을 유지한다 (Stop 훅이 의도치 않은 Reviewer 발동을 차단):

```bash
touch .claude/.review_skip
```

## 수행 절차

1. `$ARGUMENTS`가 비어 있으면 현재 큐 내용을 출력하고 종료한다.

2. `.claude/.task_queue` 파일에 다음 형식으로 한 줄 추가한다:
   ```
   - <$ARGUMENTS>
   ```

3. 추가 후 현재 큐 전체를 출력한다.

## 종료

사용자에게 다음 안내 후 응답을 종료한다:

"큐에 추가됐습니다. 더 추가하려면 `/pipeline-build <작업지시>`, 실행을 시작하려면 `/pipeline-start`."
