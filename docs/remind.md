# 운영 회고 — 메인 가이드에서 분리된 항목 보관소

> 본 문서는 `ENTRY_MIGRATION_GUIDE.md`에서 정리된 항목을 모아두는 회고/참고용 보관소다.
> Entry 화면 마이그레이션 작업과 직접 관련은 없지만, 동일 인프라/스택을 다루는 후속 작업자에게 도움이 될 수 있는 기록을 남긴다.

---

## 1. SQL UPDATE 로그가 2번 출력될 때 — p6spy 카테고리 이중 hook 의심

> 원래 `ENTRY_MIGRATION_GUIDE.md` §6.27 에 있었던 항목. 백엔드 SQL 로깅(p6spy) 진단·dedup 회고로, Entry 마이그레이션 작업자가 직접 만나는 함정이 아니라 본 문서로 이전.

p6spy 로그에 같은 UPDATE SQL이 두 번 출력되어 마치 SQL이 두 번 emit된 것처럼 보일 수 있다. **진짜 SQL emit은 1회**일 가능성이 매우 높다. ORM 측 가설(dirty cycle 재진입, AuditingEntityListener, `@MappedSuperclass`, 양방향 매핑 등)에 빠지기 전에 **반드시 진짜 SQL emit 횟수부터 측정**할 것.

**원인**:
- Hibernate `batch_size > 1` + `order_updates: true` 환경에서 UPDATE는 batch 모드로 실행
- p6spy가 `addBatch()`와 `executeBatch()` 두 시점에 hook → 같은 SQL 두 번 logging
- INSERT는 `GenerationType.IDENTITY` 사용 시 batch 처리 불가(ID가 INSERT 시점에서야 결정) → statement 카테고리 1회만 logging → 정상
- 즉 **UPDATE만 이중 출력, INSERT는 정상**이면 거의 확실히 이 케이스

**진단 방법**: `application-local.yml`에 다음 추가 후 재실행:
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        generate_statistics: true
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.stat: DEBUG
```
Hibernate `show_sql` 출력 + `Session Metrics` 의 `flushing a total of N entities` 와 p6spy 출력 횟수 비교. p6spy가 2배면 logging 이중 출력 확정.

**해결**: `PrettySqlFormatter` 에 ThreadLocal 기반 dedup (같은 connectionId의 같은 SQL이 짧은 시간 안에 또 들어오면 두 번째 skip). `spy.properties` 의 `excludecategories` 에서 `batch` 만 제외하면 UPDATE 가 아예 안 보이므로 그쪽으로는 가지 말 것.

사례: 05df425 — p6spy 카테고리 이중 출력 dedup 적용. (이전에 시도한 dirty cycle / Auditing / `@MappedSuperclass` / 양방향 매핑 가설들 전부 무효였음. 진단 없이 ORM 측 fix 들어가서 13개 commit이 헛수고가 된 사례.)

---

## 2. 관련 커밋 기록

> 원래 `ENTRY_MIGRATION_GUIDE.md` §10 에 있었던 p6spy 관련 항목 2건. Entry 마이그레이션 직접 항목이 아니라 본 문서로 이전.

- **p6spy 도입 (2026-05-09, 9980eaf)** — SQL 로그 inline 파라미터 + prettify 출력. UPDATE 이중 출력 dedup은 후속 fix(05df425·0a885c4).
- **p6spy UPDATE 이중 출력 dedup 해결 (2026-05-09, 05df425·0a885c4)** — Hibernate batch 모드 + IDENTITY ID 조합에서 UPDATE만 batch 카테고리에서 두 번 logging되는 현상. `PrettySqlFormatter` ThreadLocal dedup 으로 해결. 이전 13개 commit (Strategy A: Hibernate 네이티브 timestamp / Strategy B: AuditingEntityListener 제거 + Mapper 직접 set / Strategy C: 양방향 매핑 / `@MappedSuperclass` bisect / `updateNonBl` 트랜잭션 분리 등)은 모두 무효 시도였고 `0e55a28` 으로 hard reset 후 p6spy logging 만 수정으로 종결.

---

## 3. 교훈 (요약)

- **로깅 이중 출력은 ORM 가설을 세우기 전에 logging layer부터 의심하라.** `show_sql` 와 p6spy 출력 횟수를 직접 비교해 진단.
- ORM 가설에 빠지면 dirty cycle / Auditing / `@MappedSuperclass` / 양방향 매핑 등 가능 가설이 너무 많아 13 commit 헛수고가 발생할 수 있음.
- `excludecategories=batch` 같은 우회는 진짜 UPDATE 로그까지 사라지므로 회피.

---

## 4. 본 문서의 후속 갱신 기준

- `ENTRY_MIGRATION_GUIDE.md` 에서 정리되어 들어오는 운영/인프라 회고가 추가될 때 이 문서에 섹션 추가
- 동일 함정이 다시 재현·확장되면 본 섹션 보강
