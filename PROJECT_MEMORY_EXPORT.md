# 프로젝트 메모리 이식 패키지

**Export 일자:** 2026-05-14
**원본 위치:** `C:\Users\a0102\.claude\projects\C--vive-coding-portfolio-legacy-to-next-legacy-to-next\memory\`
**대상 프로젝트:** legacy-to-next (Spring Boot + Next.js, 헥사고널 아키텍처)
**목적:** 다른 PC의 Claude Code auto memory 시스템에 이식

---

## 이식 방법

대상 PC에서 다음 디렉토리에 각 섹션을 개별 `.md` 파일로 분리 저장한다.

```
{대상 PC의 Claude 메모리 경로}/memory/
```

Windows 기본 경로 예시:
```
C:\Users\{USERNAME}\.claude\projects\{프로젝트 슬러그}\memory\
```

각 메모리는 본 문서의 `=== FILE: {파일명}.md ===` 블록 단위로 구분되어 있다. 블록 내용(프론트매터 + 본문)을 그대로 해당 파일명으로 저장하면 된다. 마지막에 `MEMORY.md` 인덱스 파일도 동일하게 저장한다.

**주의사항:**
- 이 메모리들은 본 프로젝트(legacy-to-next)에 특화된 컨벤션이다. 다른 프로젝트에서 직접 적용하지 말 것.
- 일부 메모리는 시점 의존적(plan 예약, 진행 상황 등)이므로 이식 시점에 최신 git log/plan 상태와 대조 필요.
- `originSessionId` 필드는 원본 세션 추적용이며 이식 시 그대로 두거나 삭제해도 무방하다.

---

## 메모리 분류 요약

**프로젝트 메모리 (4개)** — 작업/이슈/계획의 현재 상태
- M1 헥사고널 아키텍처 리팩토링 완료
- 22개 fms 테이블 채움 plan
- List 화면 검색 DTO 분리 계획
- house_bl_license 테이블 제거 계획

**피드백 메모리 (22개)** — 사용자가 명시·검증한 작업 규칙
- 워크플로/에이전트: 7개
- 아키텍처/코드 패턴: 9개
- 테스트/검증/QA: 4개
- 알림/문서화: 2개

---

=== FILE: MEMORY.md ===

```markdown
- [M1 헥사고널 아키텍처 리팩토링 완료](project_m1_hexagonal.md) — 2026-04-25 완료, 신규 도메인 추가 시 4계층 패턴 참고
- [22개 fms 테이블 빈 컬럼·빈 테이블 채움 plan 예약](project_22tables_fill.md) — plan은 .claude/plans/master-bl-select-noble-map.md, 5 Phase 분할 확정 (house_bl_license는 별도 제거로 목록에서 제외됨)
- [house_bl_license 테이블 제거 작업 예약](project_house_bl_license_removal.md) — DDL+백엔드 제거, plan 승인 완료 (house-bl-license-glimmering-quasar.md)
- [정합성 이슈 및 설정 수정 작업 시 에이전트 호출 규칙](feedback_agent_scope.md) — 설정/문서/스크립트 수정은 메인 직접 처리, Coder는 도메인 코드에만
- [git commit 타이밍 규칙](feedback_git_commits.md) — 사용자 명시 요청 시에만 커밋, 자동 커밋 금지
- [CLAUDE.md 간결 유지 방침](feedback_claudemd_length.md) — 동작에 필수적이지 않은 내용은 CLAUDE.md에 추가하지 않음
- [refactor-by-rules Refactorer 위임 원칙](feedback_refactorer_delegation.md) — 메인은 판단만, 파일 Read·Edit는 Refactorer에 위임 (토큰 절약)
- [Mockito BDD 메서드명 변경 시 두 패턴 체크](feedback_mockito_rename.md) — given(port.method()와 .should().method() 모두 교체 필요, 한쪽만 하면 빌드 실패
- [Frontend-QA build FAIL 시 tsc --noEmit 전체 오류 수집](feedback_qa_tsc_noémit.md) — build는 첫 오류에서 멈추므로 FAIL 시 tsc --noEmit 추가 실행해 전체 타입 오류 한 번에 수집 (Frontend-QA.md 반영됨)
- [프론트엔드 stub은 테스트/mock 어댑터에만](feedback_frontend_stub_test_only.md) — product 컴포넌트 내 하드코딩 defaultValue 금지, dev/test 전용 mock 어댑터·테스트 파일에만 허용
- [워크트리 사용 금지](feedback_worktree_base.md) — 모든 서브 에이전트 호출 시 isolation:"worktree" 금지, 메인 작업 디렉토리에서 직접 작업
- [기본 파이프라인은 /pipeline-coder-qa](feedback_default_pipeline.md) — 사용자가 파이프라인 미지정 시 자동으로 /pipeline-coder-qa 절차 적용
- [표준 UI 컴포넌트 도입 시 living catalog 동반](feedback_ui_catalog.md) — 신규 표준 컴포넌트 PR에 (dev) 라우트 카탈로그 필수 작성
- [Backend-QA / Frontend-QA 빌드·테스트 명령어 자동 허용](feedback_qa_auto_allow.md) — lint/build/test 명령어는 settings.json allow 등록, 허가 요청 없이 진행
- [Coder plan mode 진입 시 사용자 보고 의무](feedback_coder_plan_mode_report.md) — Coder가 자체 plan mode 진입 시 메인은 즉시 사용자에게 알려야 함, 재호출 루프 금지
- [List 화면 검색 DTO 범주별 분리 계획](project_list_dto_split.md) — 6범주×12DTO 목표, Truck·Non B/L 진행 중(2026-05-05), 나머지 Sea House/Air House/Sea Master/Air Master 후속
- [UseCase create 메서드 SRP + 도메인 노출 금지](feedback_usecase_create_method.md) — createXxx는 id만 반환, 컨트롤러 var로 도메인 은폐 금지
- [Adapter(in)→Domain 직접 참조 금지](feedback_adapter_domain_direct.md) — Presentation은 Application Command/UseCase만, 계층 건너뛰기 절대 금지(ARCH1 명문화)
- [List 화면 Entry 저장 후 자동 invalidate 금지](feedback_list_entry_invalidate.md) — Entry mutation 후 List 캐시 자동 invalidate 금지, 사용자가 Search로 직접 재조회
- [Entry vs List filter scope 분리](feedback_entry_vs_listfilter_scope.md) — Entry plan에 List filter 전용 enum/필드(partyKind/dateKind/portKind) 혼용 금지, 독립 plan으로 분리
- [파이프라인 진행 상황 Discord 알림](feedback_discord_pipeline_notify.md) — Coder/QA 완료 시점마다 Discord reply로 요약 전송
- [Entry 화면 작업 시 ENTRY_MIGRATION_GUIDE.md 업데이트 의무](feedback_entry_guide_update.md) — Entry 작업 완료마다 ENTRY_MIGRATION_GUIDE.md 갱신, "종료" 신호 전까지 유지
- [validation BE SSOT 원칙](feedback_validation_be_ssot.md) — 검증 책임은 백엔드 일원화, FE zodResolver/HTML5 required 추가 금지 (관리 포인트 분산 방지)
- [Entry toolbar 식별 필드 FE readOnly 금지](feedback_toolbar_id_field_no_readonly.md) — toolbar의 hbl_no 등 식별/조회 키 필드는 update 차단을 BE Domain·Command·Request DTO에서만, FE readOnly 금지
- [비결정적(flaky) 테스트 금지](feedback_test_determinism.md) — 시간·DB 시퀀스 절대값·정렬 tie-break·외부 의존·랜덤·sleep 회피. rules/backend·frontend_coding_rules.md "# 테스트 규칙" T1·T2 SSOT
- [ENUM DB 저장값은 ENUM 인스턴스 필드명](feedback_enum_db_value.md) — code/label/number 아닌 enum.name() 사용 (외부 시드/마이그레이션/ETL)
- [FE 변경 commit 전 강력 새로고침 검증 의무](feedback_fe_verify_before_commit.md) — lint·build PASS만으로 commit 금지, 사용자 화면 검증 받은 뒤 commit (dev `.next` 캐시 stale 회피)
```

---

## 프로젝트 메모리

=== FILE: project_m1_hexagonal.md ===

```markdown
---
name: M1 헥사고널 아키텍처 리팩토링 완료
description: 백엔드/프론트엔드 헥사고널 아키텍처 리팩토링 완료 (2026-04-25). 이미 완료된 작업.
type: project
---
2026-04-25 세션에서 완료됨. 더 이상 예약 작업 없음.

**Why:** CLAUDE.md 선언과 실제 코드 구조 불일치 해소.

**결과 구조:**

백엔드: `domain/{hbl,mbl}/` (Entity/VO/도메인서비스만) + `application/{hbl,mbl}/port/in·out/` + `application/{hbl,mbl}/` + `adapter/in/web/` + `adapter/out/persistence/`
(2026-05-05 port를 application으로 이전 — 정통 헥사고널 정합)

프론트엔드 (완화된 헥사고널):
- `domain/`, `application/`, `adapter/out/api·mock/` 신규 추가
- `app/`, `components/` 위치 유지 (Next.js 제약)
- CLAUDE.md에 프론트엔드 예외 명시됨

**How to apply:** 신규 도메인 추가 시 동일 4계층 패턴 적용. 프론트엔드 API 어댑터는 zod 스키마 + 타입 에러 클래스(ApiError/NotFoundError/ResponseParseError) + fetchJson 유틸 패턴 사용.
```

=== FILE: project_22tables_fill.md ===

```markdown
---
name: 22개 fms 테이블 빈 컬럼·빈 테이블 채움 plan 예약
description: 22개 fms 테이블(master/house/switch BL) 모두를 E2E로 채우기 위한 5-Phase plan, 2026-05-04(내일)에 이어서 진행 예정
type: project
---
22개 fms 테이블의 빈 컬럼·빈 테이블을 Playwright E2E로 전부 채우는 작업이 예약되어 있다. plan 파일: `C:\Users\a0102\.claude\plans\master-bl-select-noble-map.md`. 작성일 2026-05-03.

**Why:** 사용자가 plan을 ExitPlanMode 직전에 중단하고 "내일 처리하겠다"고 명시. plan 자체는 검증 완료, 코드 작성은 미시작.

**How to apply:** 새 세션에서 사용자가 이 plan에 대해 언급하거나 "어제 plan 이어서" 같은 컨텍스트가 나오면 다음 순서로 진행.

1. plan 파일 읽기: `C:\Users\a0102\.claude\plans\master-bl-select-noble-map.md`
2. baseline 재확인: 22개 SQL 쿼리 재실행 (docker exec fms-postgres psql ...) — 1일 윈도우 이동으로 결과 달라질 수 있음
3. 코드 드리프트 확인: `git log --since="1 day ago" -- back-end/ front-end/`
4. Phase 1부터 시작 (`/pipeline-build` 또는 `/plan`으로 Phase 단위 작업 단위 등록)

**진단 결과 스냅샷 (2026-05-03 기준)**

- SYSTEM 1일 이내 0건 테이블 17개: master(air, desc, dim, schedule_leg, air_charge), house(air, truck, non_bl, desc, dim, container, schedule_leg, truck_order, air_charge), switch(switch_bl, switch_bl_description)
  - ※ house_bl_license는 별도 작업으로 테이블 자체 제거 (2026-05-04, plan: house-bl-license-glimmering-quasar.md) — 채움 대상에서 제외
- 부분 채워짐 4개 (컬럼 대부분 NULL): master_bl 14/30, master_bl_sea 15/16, house_bl 26/37, house_bl_sea 22/24

**확정된 결정 사항 (사용자 답변)**

- Phase 단위 분할 (Phase 1=백엔드 DTO/Assembler 통로, Phase 2=Switch 4계층, Phase 3=프론트 폼, Phase 4=Switch UI + truck/non_bl variant 통합, Phase 5=E2E)
- Switch B/L 신규 4계층 + UI 모두 작성 포함 (현재는 Adapter층만 존재)
- house TRUCK/NON_BL은 `[variant]` 라우트에 통합 (기존 `/fms/truck-bl`, `/fms/non-bl` 별도 페이지는 흡수 후 redirect)

**현재 시점 차단 요인 (plan에서 다루는 항목)**

- 백엔드 Create/Update DTO에 sub 엔티티 nested 필드 부재 (Adapter sync 코드는 완비)
- Switch B/L Controller/Service/DTO/Assembler 미구현
- House BL Assembler `case TRUCK, NON_BL → throw UnsupportedOperationException`
- 프론트 sub 패널 stub, TradePanel/CargoDocPanel `<span>` 라벨 + register 미연결
- E2E `phase5-e2e.spec.ts`가 sea-exp만 + CREATE는 API 직접 호출로 우회
```

=== FILE: project_list_dto_split.md ===

```markdown
---
name: List 화면 검색 DTO 범주별 분리 계획
description: House/Master/Truck/Non BL 6개 범주별 요청·응답 DTO 분리 진행 상황
type: project
---
6 범주 × (요청+응답) = 12 Summary DTO 분리 계획. 단일 `HouseBlSummary`·`HouseBlSummaryResponse`·`POST /api/house-bl/search`가 모든 jobDiv를 처리하는 구조에서 범주별 전용 endpoint·projection·DTO로 점진 이관.

분리 완료 예정 (2026-05-05 진행 중): Truck B/L, Non B/L
- `POST /api/truck-bl/search` + TruckBlSummary projection + TruckBlSummaryResponse + SearchTruckBlRequest
- `POST /api/non-bl/search` + NonBlSummary projection + NonBlSummaryResponse + SearchNonBlRequest
- 기존 `/api/house-bl/search`·`HouseBlSummary`·`searchSummaries`는 cleanup 하지 않고 유지 (후속 PR)

후속 작업 (미완):
- Sea House List: `/api/sea-house-bl/search` (현재 GET /api/house-bl, jobDiv=SEA)
- Air House List: `/api/air-house-bl/search` (현재 GET /api/house-bl, jobDiv=AIR)
- Sea Master List: projection 신설 필요 (현재 entity 직반환, QueryDSL projection 미사용)
- Air Master List: 동일

**Why:** 단일 DTO에 nullable 비대화 방지, 컴파일 타임 타입 안전성 확보, jobDiv별 도메인 전용 컬럼 표현(trucker/docPartner for Truck, vessel/voyage/liner for Sea·Air)
**How to apply:** 다른 List 화면 작업 시 동일 패턴(전용 projection+ResponseDTO+RequestDTO+endpoint+Repository 메서드 신설, 기존 코드 무변경)으로 분리 진행. 각 범주 작업 시 이 메모리 업데이트.
```

=== FILE: project_house_bl_license_removal.md ===

```markdown
---
name: house_bl_license 테이블 제거 작업 예약
description: house_bl_license(E-17 면장) 테이블을 백엔드+DDL에서 제거, plan 승인 완료, 7시간 후 실행 예정
type: project
---
프론트엔드 로직은 이미 제거 완료. 백엔드(자바) + DDL 제거만 남음. plan 승인 완료.

**Plan 파일:** `C:\Users\a0102\.claude\plans\house-bl-license-glimmering-quasar.md`

**Why:** house_bl_license는 다른 테이블의 FK 참조 없는 leaf 테이블. HouseBlPort에 license 전용 메서드도 없어 제거 범위가 명확함.

**How to apply:** 새 세션에서 이 작업을 이어받으면 plan 파일을 읽고 PIPELINE.md 순서대로 Backend-coder 순차 호출 → DDL 메인 직접 처리 → QA 실행. 파이프라인은 기본값 `/pipeline-coder-qa`.

**영향 범위 (15개 파일)**
- DDL: `schema/V1__fms_initial_schema.sql` L596–L630
- Domain: `HouseBlLicense.java`(삭제), `HouseBl.java`(licenses 컬렉션·initLicenses 제거)
- Adapter Out: `HouseBlLicenseJpaEntity.java`(삭제), `HouseBlJpaEntity.java`, `HouseBlMapper.java`, `HouseBlCargoMapper.java`, `HouseBlPersistenceAdapter.java`
- Adapter In: `CreateHouseBlRequest.java`, `UpdateHouseBlRequest.java`, `HouseBlSubAssembler.java`
- Test 5개: `HouseBlJpaEntityTest`, `HouseBlMapperTest`, `HouseBlMapperModeTest`, `HouseBlMappingIntegrationTest`, `HouseBlPersistenceAdapterTest` (plan 승인 내에 테스트 수정 포함됨)

**작업 순서**
1. Backend-coder ① Adapter Out 정리
2. Backend-coder ② Adapter In 정리
3. Backend-coder ③ Domain 정리
4. Backend-coder ④ Test 5개 정리
5. 메인 직접 — DDL L596–L630 제거
6. QA — `gradlew build` + `gradlew test`
```

---

## 피드백 메모리 — 워크플로/에이전트

=== FILE: feedback_agent_scope.md ===

```markdown
---
name: 정합성 이슈 및 설정 수정 작업 시 에이전트 호출 규칙
description: 설정·문서·스크립트 수정은 메인이 직접 처리. Coder 호출은 M1 헥사고널 리팩토링 같은 실제 도메인 코드 작업에만.
type: feedback
---
설정 파일, 문서, 스크립트 수정은 메인 에이전트가 직접 처리한다. Coder 서브에이전트 호출 금지.

**Why:** 사용자가 invoke_reviewer.sh 수정을 Coder에게 위임하려 할 때 중단. Coder 호출은 오버헤드이며, 설정/문서 수정은 메인이 직접 Read→Edit로 처리하는 게 적절.

**How to apply:** `.claude/` 설정 파일, CLAUDE.md, 에이전트 정의(.md), 스크립트(.sh) 수정 시 Coder 대신 메인이 직접 편집. Coder는 back-end/front-end 도메인 코드 작업에만 호출.
```

=== FILE: feedback_git_commits.md ===

```markdown
---
name: git commit 타이밍 규칙
description: git commit은 사용자가 명시적으로 요청할 때만. 코드 수정 후 자동 커밋 금지.
type: feedback
---
파일을 수정한 후 git commit은 하지 않는다. 사용자가 명시적으로 "커밋해줘" 또는 "commit"을 요청할 때만 실행.

**Why:** 사용자가 Coder 호출 시 commit 포함 지시를 중단하며 "git에는 뭐 하지마"라고 명시.

**How to apply:** 코드/파일 수정 완료 후 자동으로 git add/commit 하지 않는다. git 작업은 항상 사용자 명시 요청 후 실행.
```

=== FILE: feedback_claudemd_length.md ===

```markdown
---
name: CLAUDE.md 간결 유지 방침
description: CLAUDE.md는 최대한 짧게 유지. 파이프라인 동작에 영향 없는 참조 정보는 추가하지 않는다.
type: feedback
---
CLAUDE.md는 최대한 짧게 유지한다. 파이프라인 동작에 직접 영향을 주지 않는 참조 정보(파일 경로, 운영 메모 등)는 추가하지 않는다.

**Why:** 사용자가 명시적으로 "CLAUDE.md는 최대한 짧게 유지해야 해"라고 지시.

**How to apply:** CLAUDE.md 수정 제안 시 동작에 필수적인 내용인지 먼저 판단. 문서화 목적만이라면 PIPELINE.md 등 다른 파일에 위임하거나 스킵.
```

=== FILE: feedback_refactorer_delegation.md ===

```markdown
---
name: refactor-by-rules에서 Refactorer 위임 원칙
description: refactor-by-rules 흐름에서 메인은 판단만, 파일 읽기·쓰기는 Refactorer에 위임
type: feedback
---
파일 읽기(Read)와 쓰기(Edit/Write)는 Refactorer에 위임한다. 메인이 직접 Read/Edit 하면 토큰 낭비.

**Why:** 메인이 파일 읽기·수정을 직접 수행하면 컨텍스트 토큰을 불필요하게 소비한다. CLAUDE.md에도 "메인 에이전트는 오케스트레이션 역할만 담당"이라고 명시되어 있다.

**How to apply:** `refactor-by-rules` 흐름에서
- 메인: 대상 파일 Read + 위반 탐지 (Haiku는 복합 구조 위반을 놓칠 수 있으므로 탐지는 메인 책임), 게이트 판단, 후보 제시, 사용자 승인 수집
- Refactorer: 메인이 확정한 변경 사항만 Edit/Write. 탐지 역할 없음.
- Refactorer 프롬프트는 "확정 식별자 목록(구명→신명) + 대상 파일 목록"만 전달. 파일별 상세 명세 작성 금지.
- Refactorer 보고 지시에 "항목명 + 완료/미처리 여부만, 코드 블록 금지"를 항상 포함.
```

=== FILE: feedback_worktree_base.md ===

```markdown
---
name: 워크트리 사용 금지
description: 모든 서브 에이전트(Coder/Mediator/Refactorer/QA/Plan 등) 호출 시 isolation:"worktree" 옵션 사용 금지. 메인 작업 디렉토리에서 직접 작업.
type: feedback
---
서브 에이전트 호출 시 `isolation: "worktree"` 옵션을 사용하지 않는다. 모든 작업은 메인 작업 디렉토리(현 trunk HEAD)에서 직접 수행한다. 병렬 Coder 호출 역시 워크트리 없이 진행하며, 필요하면 호출 분리·순차화로 대응한다.

**Why:** 2026-05-10 Backend-coder 호출 시 워크트리 base가 옛 commit(`4572a33`)으로 잡혀 master HEAD(`9452efc`)와 5커밋 차이가 발생, stash+reset+pop으로 base를 끌어올린 뒤 9개 파일 충돌을 수동 해결하는 사고가 났음. 사용자가 동일 위험을 원천 차단하기 위해 워크트리 사용 자체를 금지함.

**How to apply:**
1. Agent 호출 시 `isolation` 파라미터를 절대 지정하지 않는다.
2. 백엔드 Coder 작업 결과는 메인 작업 디렉토리에 직접 반영되며, 사용자 명시 요청이 있을 때만 commit·merge.
3. 프론트엔드 Coder는 백엔드 commit이 master에 반영된 상태에서 master HEAD 위에서 작업.
4. `feedback_default_pipeline.md` 등 기존 파이프라인 메모는 그대로 유효하나, 워크트리 절차는 무시.
5. `.claude/commands/refactor-by-rules.md`의 worktree 옵션 절차도 폐기됨(2026-05-10 갱신).
```

=== FILE: feedback_default_pipeline.md ===

```markdown
---
name: 기본 파이프라인은 /pipeline-coder-qa
description: 사용자가 파이프라인을 따로 지정하지 않은 모든 코드 변경 작업의 기본 실행 경로
type: feedback
---
사용자가 파이프라인 종류(`/pipeline`, `/pipeline-build`, `/pipeline-start`, `/pipeline-review` 등)를 명시하지 않은 코드 변경 작업은 자동으로 `/pipeline-coder-qa`로 진행한다.

**Why:** 사용자가 2026-05-03 세션에서 명시적으로 지정. Reviewer 미발동 + Planner 미발동의 단축 사이클이 일반 작업에 적합하다고 판단.

**How to apply:** 사용자가 코드 변경을 요청하고 슬래시 커맨드로 다른 파이프라인을 명시하지 않으면, 기본적으로 `/pipeline-coder-qa` 절차로 처리:
- `.claude/.review_skip` sentinel 생성
- Planner 호출 금지
- Coder × N (병렬 가능 시) → 트렁크 머지 → QA → worktree 정리 → sentinel 정리
- `.review_pending` 마커 생성 금지 (Reviewer 미발동)

다른 파이프라인이 더 적합해 보이더라도(예: 기획 정리가 필요해 보이는 큰 작업), 사용자 지시 우선. 의심스러우면 사용자에게 확인.
```

=== FILE: feedback_coder_plan_mode_report.md ===

```markdown
---
name: Coder plan mode 진입 시 사용자 보고 의무
description: Frontend/Backend-coder가 자체 plan mode에 진입하면 사용자에게 즉시 알려야 함
type: feedback
---
Coder 에이전트가 plan mode에 진입해 승인 대기 상태가 되면, 메인은 사용자에게 즉시 알리고 기다려야 한다. 에이전트를 재호출하거나 혼자 루프를 돌지 말 것.

**Why:** 사용자가 직접 지적 — "coder 호출되었는데 plan mode면 바꿔달라고 말을 해 혼자서 돌고 있지말고"

**How to apply:** Coder 결과 메시지에 "ExitPlanMode" 또는 "plan mode 활성" 등이 보이면, 다음 행동은 반드시 사용자에게 상황 보고 후 대기. 메인이 직접 Edit/Write로 대신 처리하는 것은 OK (공유 컴포넌트나 간단 수정이면 메인 직접 처리 고려).
```

---

## 피드백 메모리 — 아키텍처/코드 패턴

=== FILE: feedback_usecase_create_method.md ===

```markdown
---
name: UseCase create 메서드는 생성만, 도메인 객체 컨트롤러 노출 금지
description: createXxx UseCase 메서드는 SRP상 생성만 담당(void 또는 id 반환). 컨트롤러가 도메인 객체를 var로 받아 은폐하는 패턴 금지.
type: feedback
---
UseCase의 createXxx 메서드는 생성 책임만 담당해야 한다. 도메인 객체를 반환하면 생성+조회 두 책임이 되어 SRP 위반.

**Why:** 사용자 지적 - createHouseBl이 HouseBl을 반환하면 "생성과 반환을 같이 해버려 하나의 메서드가 두 가지 일을 한다". var로 도메인 타입을 은폐하는 것도 근본 문제(컨트롤러가 도메인 객체 직접 접근)를 숨기는 것.

**How to apply:**
- createXxx → `Long id` 반환 (생성 후 ID만). 컨트롤러는 id로 URI 생성, 응답 바디는 별도 findXxxById 호출
- 도메인 객체를 컨트롤러에서 var로 받으면 안 됨. 도메인이 컨트롤러(프레젠테이션)에 직접 노출되는 구조 자체를 수정해야 함
- Domain → Application(UseCase) → Adapter(in/Controller) 계층 건너뛰기 금지
```

=== FILE: feedback_adapter_domain_direct.md ===

```markdown
---
name: Adapter(in) → Domain 직접 참조 금지
description: Presentation 계층이 Domain을 직접 참조하면 Application 계층 존재 의미가 없어짐. DTO→Command는 Assembler, Command→Entity는 Factory/Service.
type: feedback
---
Adapter(in)(Controller/Assembler)은 Domain 레이어를 직접 import하지 않는다.

**Why:** 사용자 지적 - Presentation이 Domain을 직접 쓰면 Application 계층을 만들 이유가 없다. 계층 간 호출 준수는 헥사고널 아키텍처의 핵심. rules/backend_coding_rules.md ARCH1에 명문화됨(2026-05-06).

**How to apply:**
- Assembler: DTO → Command(primitive record, application 레이어) 변환만. domain.* import 금지.
- Application Factory: Command → 도메인 Entity/VO 변환 책임
- Application Service: 트랜잭션 + Factory 위임 + Port 호출
- HouseBlFilter 등 domain 검색 객체도 application SearchQuery로 대체 검토
```

=== FILE: feedback_frontend_stub_test_only.md ===

```markdown
---
name: 프론트엔드 stub은 테스트/mock 어댑터에만
description: 프론트엔드 product 컴포넌트에 하드코딩 stub을 넣지 않는다 — 테스트/mock에만 허용
type: feedback
---
프론트엔드 stub 데이터(하드코딩 defaultValue, 인라인 default 객체)는 product 컴포넌트에 잔존해서는 안 된다.

**Why:** Entry 화면 진입 시 "COSCO2404195", "한진무역(주)" 등 고정 값이 깔려있는 문제가 발견됐고, 사용자가 명시적으로 "프로덕트 코드에는 섞이지 않게 해야해"를 요구. 향후 PR 리뷰 기준으로도 적용.

**How to apply:**
- 컴포넌트 내부 `DEFAULTS_*` 객체 값, `defaultValue="실제값"` JSX prop, 인라인 삼항 defaultValue → 모두 `""` 또는 제거
- 시연·개발용 fixture는 `adapter/out/mock/*` 또는 테스트 파일로만 이동
- `NEXT_PUBLIC_USE_MOCK=true`일 때만 활성화되는 mock 어댑터는 허용 (dev/test 환경 전용이므로)
- "TODO: stub 유지" 주석이 달린 곳도 예외 없이 적용
```

=== FILE: feedback_list_entry_invalidate.md ===

```markdown
---
name: List 화면 Entry 저장 후 자동 invalidate 금지
description: Entry(생성/수정/삭제) 성공 후 List queryKey를 자동 invalidate하지 않음. 업무 사용자가 Search로 직접 재조회.
type: feedback
---
Entry(생성/수정/삭제) mutation 성공 콜백에서 List 캐시를 자동 invalidate하지 않는다.

**Why:** 업무 사용자가 List로 돌아왔을 때 직접 Search 버튼을 눌러 재조회하는 흐름이 의도된 동작. 자동 invalidate는 불필요한 백엔드 호출을 유발할 수 있고, 사용자가 재조회 타이밍을 직접 제어하는 것이 맞다.

**How to apply:** truck-bl, non-bl 등 List+Entry 구조를 가진 화면에서 Entry mutation onSuccess에 `qc.invalidateQueries({ queryKey: ['...', 'list'] })`를 추가하려는 경우 이 규칙을 우선 확인. 자동 invalidate 대신 사용자가 Search로 재조회하도록 둔다.
```

=== FILE: feedback_entry_vs_listfilter_scope.md ===

```markdown
---
name: Entry 화면 plan과 List filter 영역 혼용 금지
description: Entry 화면 리팩토링 plan에 List filter 전용 enum/필드를 섞지 말 것
type: feedback
---
Entry 화면 plan에 List filter 전용 필드나 enum을 포함하지 말 것.

**Why:** Non B/L Entry는 Party 5종을 고정 라벨로 개별 입력받고, 날짜/포트도 단일 필드로 입력받는 구조다. `partyKind`, `dateKind`, `portKind` 드롭다운 방식은 List filter(`non-bl-list-filter.tsx`)에서만 사용하는 패턴이며 Entry 화면에는 존재하지 않는다. Entry plan에 `DateKind/PartyKind/PortKind` enum 등록이나 관련 필드 처리를 포함하는 것은 scope 오염.

**How to apply:** Entry 화면과 List filter는 서로 독립된 plan으로 분리. Entry plan을 작성할 때 List filter 파일(`non-bl-list-filter.tsx`)의 하드코딩/ENUM 이슈는 별도 plan에서 처리.
```

=== FILE: feedback_validation_be_ssot.md ===

```markdown
---
name: validation BE SSOT 원칙
description: 검증 책임은 백엔드에 일원화한다 — FE/BE 공존 시 관리 포인트 분산이 RTT 비용보다 비쌈
type: feedback
---
검증 책임은 백엔드(adapter/in Request DTO의 jakarta-validation)에 일원화한다. FE에 zodResolver, HTML5 required 등 별도 검증을 추가하지 않는다.

**Why:** BE/FE에 검증이 공존하면 관리 포인트가 분산되어 일관성 유지가 어려워진다. RTT 비용은 크지 않으므로 FE에서 차단할 필요가 없다.

**How to apply:**
- Request DTO에 `@NotBlank`/`@NotNull`/`@Size`/`@Pattern` 등 jakarta-validation 어노테이션으로 1차 검증.
- FE는 시각적 `is-required` 라벨 표시만 담당하고, zodResolver/handleSubmit 검증 로직 추가 금지.
- 검증 실패 시 `MethodArgumentNotValidException` → `GlobalExceptionHandler` → `ProblemDetail` → 프론트 `fetchJson` → `ApiError` → 글로벌 `MutationCache.onError` → `toast.error` 흐름은 이미 완성된 인프라이므로 어노테이션 복원만으로 자동 동작.
```

=== FILE: feedback_toolbar_id_field_no_readonly.md ===

```markdown
---
name: Entry toolbar 식별 필드 FE readOnly 금지
description: Entry 화면 toolbar의 식별 키 필드(hbl_no 등)는 FE readOnly·disabled 금지 — update 차단은 BE에서만
type: feedback
---
Entry 화면 toolbar에 있는 식별 키 필드(예: House B/L 계열의 `hbl_no`/`nonBlNo`)는 신규 저장 입력뿐 아니라 **조회 키 입력**(저장된 데이터를 불러오기 위한 입력) 용도로도 사용된다. 따라서 edit mode일 때도 readOnly·disabled를 적용하면 조회 입력이 막힌다 — FE에는 어떠한 readOnly도 걸지 말 것.

업데이트 시 해당 컬럼이 변경되지 않게 하려면 **백엔드에서만 본 차단**:
- Domain의 update PATCH 라인에서 해당 필드 제거
- Application Command, Presentation Request DTO에서 해당 필드 제거 (Jackson FAIL_ON_UNKNOWN_PROPERTIES=false 기본값으로 클라이언트가 보내도 무시됨)

**FE submit payload는** update 시점에 식별 키를 빼서 보내는 게 깨끗하지만, 다음 패턴은 금지:
- `isEdit: boolean` 인자로 build 함수 분기 — 인자가 그 한 가지 용도뿐이면 죽은 코드. 사용자가 직접 거부함.
- toolbar TextBox에 readOnly·disabled 부여 — 조회 입력이 막힘.

**대신** update 전용 빌더 함수를 별도 export(예: `buildXxxUpdateRequest`)해 내부에서 `const { hblNo: _hblNo, ...rest } = buildXxxRequest(...); return rest;` 로 destructure로 제외. entry mutation에서 create/update 분기 시 각자 다른 빌더 호출.

**Why:** 사용자가 "프론트엔드에서 hbl_no 필드에 readOnly를 걸면 안 되지. 조회 때문에 수정은 항상 가능해야 해" + "굳이 update 분기에서 hblNo를 보낼 이유가 있나?" + "isEdit가 hbl_no에만 사용되는 변수고 인자로 쓰고 있으면 사용처를 없애"라고 명시. toolbar 필드는 입력·조회·신규 저장의 3중 역할 + 단일 용도 인자는 죽은 코드.

**How to apply:** Entry 화면에서 "특정 컬럼 update 차단" 요구 시, BE Domain·Command·Request DTO 3계층에서 필드 제거(SSOT). FE는 toolbar readOnly 금지 + update payload는 별도 update 빌더에서 destructure로 제외. 단순 식별 키가 아닌 일반 필드(예: 거래처명)에는 적용 X — 이 규칙은 toolbar의 식별/검색 키 필드에 한정.
```

=== FILE: feedback_enum_db_value.md ===

```markdown
---
name: feedback-enum-db-value
description: ENUM 컬럼의 DB 저장값은 무조건 ENUM 인스턴스 필드명(name()). code/label/number는 표시용 보조 필드
type: feedback
---

ENUM 바인딩 컬럼에 데이터를 넣을 때(외부 데이터 생성기, 시드 SQL, 마이그레이션, ETL 등) **반드시 ENUM 인스턴스 필드명**(Java `enum X { A, B }`의 `A`/`B`)을 사용한다.

**Why:** 본 프로젝트 백엔드의 모든 도메인 ENUM은 `@Enumerated(EnumType.STRING)`로 매핑돼 있어 DB에는 `name()`이 저장됨. 일부 ENUM이 갖는 `code`/`label`/`number` 필드는 UI 표시·외부 API 호환용 별도 필드일 뿐 DB 저장값이 아니다.
- 예 1) `ContainerType.T20GP` — code `"20GP"`, label `"20'GP"`, **DB**: `T20GP`
- 예 2) `NoOfBl.THREE` — number `"3"`, **DB**: `THREE`
- 예 3) `BlType.SEAWAY` — label `"Sea-Way Bill"`, **DB**: `SEAWAY`
- 예 4) `CargoType.NR`, `HandlingInfoCode.A`, `Per.SHP`, `Fhd.N` — 모두 `name()` 그대로 저장

**How to apply:** ENUM 컬럼 채울 풀을 만들 때 BE Java enum 파일을 열고 `public enum X { A, B, C, ... }`의 A/B/C만 추출해 풀로 사용. 절대로 `getCode()`/`getLabel()` 반환값이나 legacy 한글 라벨·숫자 표기로 채우지 말 것. 외부 도구의 Explore/조사 결과가 "code 값을 DB에 저장한다"고 보고해도 신뢰하지 말고 직접 ENUM 정의 + `@Enumerated` 어노테이션을 확인.

관련: [[project-22tables-fill]]
```

=== FILE: feedback_ui_catalog.md ===

```markdown
---
name: 표준 UI 컴포넌트 도입 시 living catalog 동반
description: 새 표준 컴포넌트 도입 PR에 dev 라우트 카탈로그를 함께 작성하는 원칙
type: feedback
---
새 표준 UI 컴포넌트를 도입할 때는 반드시 living catalog(dev 라우트)를 함께 작성한다.

**Why:** 카탈로그가 있으면 Claude가 신규 컴포넌트 상태를 브라우저에서 직접 확인할 수 있고, 사용자도 표준 reference로 활용할 수 있다. "스탠다드로 삼아서 네가 가져다 쓰기도 좋잖아?" — 사용자 확인.

**How to apply:** 표준 컴포넌트 도입 PR에 `front-end/src/app/(dev)/<name>-preview/page.tsx`를 함께 생성. 이 프로젝트에서는 Storybook 미설치이므로 Next.js dev 라우트 그룹 `(dev)`를 사용. 컴포넌트 × variant(panel/cell) × 상태(required/readOnly/disabled) × RHF 연동 여부 조합을 한 페이지에 나열해 시각 확인 가능하게 함.
```

---

## 피드백 메모리 — 테스트/검증/QA

=== FILE: feedback_mockito_rename.md ===

```markdown
---
name: Mockito BDD 메서드명 변경 시 두 패턴 모두 체크
description: 포트/인터페이스 메서드 rename 시 given()과 then().should() 패턴을 모두 교체해야 함
type: feedback
---
mock 메서드명을 변경할 때 `given(port.method(` 패턴만 교체하면 `then(port).should().method(` 패턴이 누락돼 빌드 실패한다.

**Why:** Mockito BDD 스타일에서 `given()` 안의 호출은 `port.method(` 형태지만 `then().should()` 안의 호출은 `.should().method(` 형태로 구분되어 replace_all 패턴이 달라야 함.

**How to apply:** 인터페이스 메서드명 변경 후 테스트 파일 수정 시 반드시 두 패턴 모두 교체:
1. `port.oldMethod(` → `port.newMethod(`
2. `.should().oldMethod(` → `.should().newMethod(`
```

=== FILE: feedback_qa_tsc_noémit.md ===

```markdown
---
name: Frontend-QA build FAIL 시 tsc --noEmit 전체 오류 수집
description: npm run build는 첫 번째 타입 오류에서 멈추므로 build FAIL 시 tsc --noEmit을 추가 실행해 전체 타입 오류를 한 번에 수집해야 함
type: feedback
---
build FAIL 시 `npx --prefix front-end tsc --noEmit`을 추가로 실행해 모든 타입 오류를 한 번에 수집한다. 이미 Frontend-QA.md에 반영됨.

**Why:** TypeScript 컴파일러는 첫 번째 오류에서 멈추는 특성이 있어, `npm run build` 출력만으로는 같은 파일 내 다른 오류가 보이지 않는다. 이로 인해 같은 파일을 여러 번 수정하는 비효율이 발생했음 (mock/house-bl.ts를 4번 수정한 사례).

**How to apply:** Frontend-QA가 build FAIL을 감지하면 tsc --noEmit을 추가 실행해 전체 타입 오류 목록을 확보하고, Frontend-coder에게 한 번에 전달.
```

=== FILE: feedback_qa_auto_allow.md ===

```markdown
---
name: Backend-QA / Frontend-QA 빌드·테스트 명령어 자동 허용
description: Backend-QA / Frontend-QA가 실행하는 lint/build/test 명령어는 허가 요청 없이 진행
type: feedback
---
Backend-QA / Frontend-QA가 실행하는 빌드·테스트 명령어는 허가 요청 없이 자동 진행한다.

**Why:** 사용자 명시 지시. QA 흐름에서 불필요한 중단을 방지하기 위해.

**How to apply:** 다음 명령어들은 .claude/settings.json의 permissions.allow에 이미 등록됨:
- npm --prefix front-end run lint  (Frontend-QA)
- npm --prefix front-end run build  (Frontend-QA)
- npm --prefix front-end run test / npm --prefix front-end test *  (Frontend-QA)
- npx --prefix front-end tsc --noEmit  (Frontend-QA)
- back-end/java-spring/gradlew.bat -p back-end/java-spring test  (Backend-QA)
- back-end/java-spring/gradlew.bat -p back-end/java-spring build  (Backend-QA)

신규 Backend-QA / Frontend-QA 명령어가 추가될 경우 settings.json permissions.allow에 함께 추가할 것.
```

=== FILE: feedback_test_determinism.md ===

```markdown
---
name: feedback-test-determinism
description: "비결정적(flaky) 테스트 작성 금지. 시간·시퀀스 절대값·정렬 tie-break·외부 의존·랜덤·sleep 회피. flaky 발견 시 검증 완화 금지, 별도 PR로 안정화."
type: feedback
---

코드 정확성을 검증하는 테스트가 자기 자신부터 결정적이어야 한다. Coder는 테스트를 신규 작성하거나 기존 테스트를 수정할 때 항상 결정성을 검증한다.

**Why:** 2026-05-12 Truck B/L address 풀스택 보강(commit 41205f8) 후 `TruckBlFindByHblNoIntegrationTest.findTruckBlKeysByHblNoExact_twoDuplicates_returnsTwo_orderedByCreatedAtDesc` 가 `expected: 12L but was: 11L` 로 실패. 원인은 이번 변경이 아니라 `persistTruckAndFlush` 두 번 연속 호출 시 createdAt 이 ms 동일 → `ORDER BY createdAt DESC` tie-break 미정의로 정렬이 깨진 flaky 테스트. 단일 격리 실행은 통과, 다른 테스트와 같이 돌면 실패. Coder 가 작성 시점에 결정성 검증을 누락하면 회귀 진단이 매번 오염된다.

**How to apply:**

- 신규 테스트 작성 시 다음 신호는 즉시 결정적 패턴으로 대체:
  - 시간 의존(`Instant.now()` / `Date.now()`) → `Clock.fixed` / `vi.useFakeTimers()`
  - DB 시퀀스 절대값(`isEqualTo(12L)`) → `isEqualTo(saved.getId())` 동적 참조 (H2 IDENTITY 시퀀스는 `@DataJpaTest` 롤백되지 않음)
  - 정렬 tie-break 미정의 → `ORDER BY createdAt DESC, id DESC` 등 secondary key 동반
  - 다른 테스트 사이드이펙트(공용 캐시 / static / DB 잔여 / localStorage) 의존 → beforeEach 명시 초기화
  - 외부 시스템·랜덤 → WireMock / MSW / seed 고정
  - `Thread.sleep` / `setTimeout` 으로 비동기 기다리기 → `Awaitility` / `waitFor` 명시 condition
- 기존 flaky 가 발견되면 **검증값 완화·sleep·retry 로 회피 금지**(의도 손실). 별도 PR 로 결정성 확보 후 본 작업 진행.
- 테스트 수정·삭제는 사용자 승인 의무([[feedback-agent-scope]] / CLAUDE.md) 그대로 적용. Coder 단독 판단으로 의도를 약화시키지 않는다.

**SSOT:** `rules/backend_coding_rules.md` 및 `rules/frontend_coding_rules.md` 의 "# 테스트 규칙" 섹션 (T1·T2). Coder 가 코드 작성 전 참조한다.
```

=== FILE: feedback_fe_verify_before_commit.md ===

```markdown
---
name: feedback-fe-verify-before-commit
description: FE 동작 변경 시 lint·build PASS만으로 commit 금지 — 강력 새로고침 후 사용자 화면 검증 받고 commit
type: feedback
---

FE 동작에 영향이 있는 변경(컴포넌트/훅/캐시 invalidate/form.reset 흐름 등)은 lint·build PASS만 가지고 commit하지 말 것. **사용자에게 `Ctrl+Shift+R` 강력 새로고침 또는 `.next` 캐시 삭제 후 화면 검증을 명시적으로 요청**하고, "정상" 확인을 받은 뒤에 commit한다.

**Why:** 2026-05-14 Sea House Entry Change B/L No invalidate 중복 제거 fix에서 lint·build PASS 후 곧바로 commit. 사용자가 dev `.next` 캐시 stale 상태에서 검증해 "여전히 미동작" 보고. 추가 race condition을 의심해 시간을 들였지만, 실제로는 fix가 맞았고 캐시만 정리하면 정상이었음. 가이드 §6.49 ⑦에 SSOT가 있는데도 commit 워크플로에서 빠뜨림.

**How to apply:**
- FE 변경 commit 직전 마지막 단계로 사용자에게: "이 화면에서 `Ctrl+Shift+R` 후 [재현 단계] 동작 확인 부탁드립니다" 명시.
- 사용자 응답 "정상/문제 없음" 확인 후에만 commit 진행. lint·build PASS는 코드 정합성 검증일 뿐 동작 검증 아님.
- 사용자가 "여전히 안 됨" 보고 시: (1) `.next` 캐시 정리 + 강력 새로고침으로 재검증 (2) localStorage stored draft 확인 (3) incognito 창 재시도 — 이 셋을 추가 fix 시도보다 먼저 제안.
- 사용자가 "X 됐어/안 됐어" 같은 추가 정보를 줄 때, fix 적용 후의 검증 결과인지 명시적으로 확인 (검증 환경/시점 가정 금지).

관련: [[feedback-git-commits]], 가이드 §6.49 ⑦ (dev server 캐시 stale)
```

---

## 피드백 메모리 — 알림/문서화

=== FILE: feedback_discord_pipeline_notify.md ===

```markdown
---
name: 파이프라인 진행 상황 Discord 알림
description: 파이프라인 주요 단계 완료 시 Discord로 요약 전송하는 행동 규칙
type: feedback
---
파이프라인 주요 단계마다 `mcp__plugin_discord_discord__reply` 도구로 Discord에 요약 알림을 전송한다.

**알림 시점과 내용:**
- **Coder 구현 완료** → "✅ [도메인] 구현 완료 — 변경 파일 N개, 주요 내용 1줄"
- **QA PASS** → "✅ QA 통과 — lint/build/test 모두 PASS"
- **QA FAIL** → "⚠️ QA FAIL — 블로커: [오류 요약], 재작업 중"
- **전체 작업 완료** → "🎉 작업 완료 — [작업명] 커밋 완료"

**Why:** 사용자가 디스코드에서 진행 상황을 실시간으로 파악하고 싶어함. 승인 요청 외에도 상태 업데이트를 받기 원함.

**How to apply:** pipeline-coder-qa, pipeline 스킬 실행 시 각 단계 완료 후 reply 도구로 위 형식에 맞춰 전송. chat_id는 메시지가 들어온 채널 기준. 디스코드 메시지 없이 세션 내 작업 중일 때도 적용.
```

=== FILE: feedback_entry_guide_update.md ===

```markdown
---
name: Entry 화면 작업 시 ENTRY_MIGRATION_GUIDE.md 업데이트 의무
description: Entry 화면 작업(BE/FE 포함) 완료마다 ENTRY_MIGRATION_GUIDE.md에 반드시 반영
type: feedback
---
Entry 화면 관련 작업이 완료될 때마다 `ENTRY_MIGRATION_GUIDE.md`에 작업 내용(함정·패턴·결정)을 업데이트한다.

**Why:** 사용자가 "Entry 화면 작업 종료했으니 메모리 정리하라"고 말하기 전까지 이 규칙은 유지.

**How to apply:** 작업(QA PASS·commit) 완료 후 가이드 갱신 → docs commit.
```

---

## 부록 — 핵심 프로젝트 컨벤션 (메모리 외부 SSOT)

이 메모리들이 실제로 작동하려면 아래 SSOT 파일들도 함께 참조 가능해야 한다. 본 export에는 포함되지 않으니 별도로 확보:

- **CLAUDE.md** — 빌드 명령어, 디렉토리 구조, 핵심 규칙(300/500줄 분리 룰, 테스트 수정 승인 룰, 워크트리 금지 룰)
- **.claude/agents/PIPELINE.md** — 파이프라인 흐름 책임 분담
- **rules/backend_coding_rules.md** — ARCH1(계층 호출), T1/T2(테스트 결정성) 등
- **rules/frontend_coding_rules.md** — 동일
- **rules/master_coding_rules.md** — refactor-by-rules SSOT
- **ENTRY_MIGRATION_GUIDE.md** — Entry 화면 마이그레이션 누적 컨텍스트
- **.claude/plans/*.md** — 진행 중 plan들 (예: `master-bl-select-noble-map.md`, `house-bl-license-glimmering-quasar.md`)

---

## 이식 체크리스트

대상 PC에서 다음을 확인:

- [ ] `.claude/projects/{프로젝트 슬러그}/memory/` 디렉토리 생성
- [ ] 각 `=== FILE: xxx.md ===` 블록을 해당 파일명으로 저장 (총 27개: 인덱스 1 + 프로젝트 4 + 피드백 22)
- [ ] `MEMORY.md`가 인덱스 역할로 작동하는지 확인 (CLI 시작 시 자동 로드되는지)
- [ ] `CLAUDE.md` 프로젝트 루트에 있는지 확인 (없으면 별도 이식)
- [ ] `rules/` 디렉토리, `.claude/agents/` 디렉토리, `ENTRY_MIGRATION_GUIDE.md` 동반 이식 여부 결정
- [ ] 이식 후 첫 세션에서 "메모리 잘 로드됐나?" 확인 — Claude에게 "현재 가지고 있는 프로젝트 메모리 요약해줘" 요청

**Export 끝.**
