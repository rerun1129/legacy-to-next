# Claude 행동 규칙 — 다른 PC 메모리 적재용

> 이 파일은 Claude Code 작업 시 지켜야 할 피드백 규칙 모음이다.
> 새 PC에서 작업 시작 전 이 내용을 Claude 메모리에 적재하면 동일한 작업 패턴을 유지할 수 있다.
> 적재 방법: 각 항목을 `/remember` 명령으로 입력하거나, 메모리 파일을 직접 복사.

---

## [CRITICAL] Adapter(in) → Domain 직접 참조 금지

Adapter(in)(Controller/Assembler)은 Domain 레이어를 직접 import하지 않는다.

**Why:** Presentation이 Domain을 직접 쓰면 Application 계층을 만들 이유가 없다. 계층 간 호출 준수는 헥사고널 아키텍처의 핵심. `rules/backend_coding_rules.md` ARCH1에 명문화됨(2026-05-06).

**How to apply:**
- Assembler: DTO → Command(primitive record, application 레이어) 변환만. `domain.*` import 금지.
- Application Factory: Command → 도메인 Entity/VO 변환 책임
- Application Service: 트랜잭션 + Factory 위임 + Port 호출
- HouseBlFilter 등 domain 검색 객체도 application SearchQuery로 대체 검토

---

## [CRITICAL] 워크트리 base 정합성 강제 검증

서브 에이전트를 `isolation: "worktree"`로 호출한 직후, 메인은 반드시 `git -C <worktreePath> rev-parse HEAD`와 `git rev-parse HEAD`(trunk HEAD)를 비교한다. 두 값이 다르면 Coder 시작 전 `git -C <worktreePath> rebase <trunk-branch>`로 정렬. rebase 충돌 시 호출 취소 후 사용자 보고.

**Why:** 2026-05-03 Non B/L 작업에서 워크트리 base가 trunk보다 5커밋 뒤처진 상태로 진행해 merge 후 무관한 화면들이 덮어쓰여진 사고 발생. 이미 `CLAUDE.md`와 `PIPELINE.md`에 영구 등록됨.

**How to apply:**
1. Agent 호출 결과의 `worktreePath` 받자마자 HEAD 비교
2. 다르면 rebase 먼저
3. 충돌 → 취소 + 사용자 보고. 자동 진행 절대 금지
4. 병렬 Coder 다수 호출 시 모든 Coder가 동일 trunk HEAD에서 분기되도록 호출 시점 정렬

---

## UseCase create 메서드 SRP + 도메인 객체 컨트롤러 노출 금지

UseCase의 `createXxx` 메서드는 생성 책임만 담당한다. 도메인 객체를 반환하면 생성+조회 두 책임이 되어 SRP 위반.

**Why:** "createHouseBl이 HouseBl을 반환하면 생성과 반환을 같이 해버려 하나의 메서드가 두 가지 일을 한다." `var`로 도메인 타입을 은폐하는 것도 근본 문제(컨트롤러가 도메인 객체 직접 접근)를 숨기는 것.

**How to apply:**
- `createXxx` → `Long id` 반환 (생성 후 ID만). 컨트롤러는 id로 URI 생성, 응답 바디는 별도 `findXxxById` 호출
- 도메인 객체를 컨트롤러에서 `var`로 받으면 안 됨
- Domain → Application(UseCase) → Adapter(in/Controller) 계층 건너뛰기 금지

---

## git commit 타이밍 규칙

파일을 수정한 후 git commit은 하지 않는다. 사용자가 명시적으로 "커밋해줘"를 요청할 때만 실행.

**Why:** "git에는 뭐 하지마" — 사용자 명시 지시.

**How to apply:** 코드/파일 수정 완료 후 자동으로 `git add/commit` 하지 않는다. git 작업은 항상 사용자 명시 요청 후 실행.

---

## 기본 파이프라인은 /pipeline-coder-qa

사용자가 파이프라인을 따로 지정하지 않은 코드 변경 작업의 기본 실행 경로.

**Why:** 2026-05-03 세션에서 명시적으로 지정. Reviewer·Planner 미발동의 단축 사이클이 일반 작업에 적합.

**How to apply:** 사용자가 슬래시 커맨드로 다른 파이프라인을 명시하지 않으면 `/pipeline-coder-qa` 절차로 처리. 의심스러우면 사용자에게 확인.

---

## 설정·문서·스크립트 수정은 메인 직접 처리

`.claude/` 설정 파일, CLAUDE.md, 에이전트 정의(.md), 스크립트(.sh) 수정 시 Coder 대신 메인이 직접 편집. Coder는 `back-end/front-end` 도메인 코드 작업에만 호출.

**Why:** 설정/문서 수정은 Coder 호출이 오버헤드. 메인이 직접 Read→Edit로 처리하는 게 적절.

---

## Coder plan mode 진입 시 사용자 보고 의무

Coder 에이전트가 plan mode에 진입해 승인 대기 상태가 되면, 메인은 사용자에게 즉시 알리고 기다려야 한다. 에이전트 재호출·혼자 루프 금지.

**Why:** "coder 호출되었는데 plan mode면 바꿔달라고 말을 해 혼자서 돌고 있지말고" — 사용자 지적.

**How to apply:** Coder 결과 메시지에 "ExitPlanMode" 또는 "plan mode 활성" 등이 보이면 반드시 사용자에게 상황 보고 후 대기.

---

## QA build FAIL 시 tsc --noEmit 전체 오류 수집 (프론트엔드)

build FAIL 시 `npx --prefix front-end tsc --noEmit`을 추가로 실행해 모든 타입 오류를 한 번에 수집한다.

**Why:** TypeScript 컴파일러는 첫 번째 오류에서 멈추므로 `npm run build` 출력만으로는 전체 오류를 알 수 없다. 같은 파일을 여러 번 수정하는 비효율이 발생했음(mock/house-bl.ts 4번 수정 사례).

**How to apply:** QA 에이전트가 build FAIL 감지 시 `tsc --noEmit` 추가 실행 → 전체 타입 오류 목록 확보 → Coder에게 한 번에 전달. (QA.md에 반영됨)

---

## Mockito BDD 메서드명 변경 시 두 패턴 모두 체크

mock 메서드명 변경 시 `given(port.method(` 패턴만 교체하면 `then(port).should().method(` 패턴이 누락돼 빌드 실패한다.

**How to apply:** 인터페이스 메서드명 변경 후 테스트 파일 수정 시 반드시 두 패턴 모두 교체:
1. `port.oldMethod(` → `port.newMethod(`
2. `.should().oldMethod(` → `.should().newMethod(`

---

## 프론트엔드 stub은 테스트/mock 어댑터에만

프론트엔드 product 컴포넌트에 하드코딩 stub(defaultValue, 인라인 default 객체)을 넣지 않는다.

**Why:** Entry 화면에서 "COSCO2404195", "한진무역(주)" 등 고정 값이 프로덕트 코드에 잔존하는 문제 발견. "프로덕트 코드에는 섞이지 않게 해야해" — 사용자 명시.

**How to apply:**
- 컴포넌트 내부 `DEFAULTS_*` 객체 값, `defaultValue="실제값"` JSX prop → `""` 또는 제거
- 시연/개발용 fixture는 `adapter/out/mock/*` 또는 테스트 파일로만 이동
- `NEXT_PUBLIC_USE_MOCK=true`일 때만 활성화되는 mock 어댑터는 허용

---

## refactor-by-rules에서 Refactorer 위임 원칙

`refactor-by-rules` 흐름에서 파일 읽기(Read)와 쓰기(Edit/Write)는 Refactorer에 위임한다. 메인이 직접 Read/Edit 하면 토큰 낭비.

**How to apply:**
- 메인: 위반 탐지 + 게이트 판단 + 사용자 승인 수집
- Refactorer: 메인이 확정한 변경 사항만 Edit/Write
- Refactorer 프롬프트에 "항목명 + 완료/미처리 여부만, 코드 블록 금지"를 항상 포함

---

## 표준 UI 컴포넌트 도입 시 living catalog 동반

새 표준 UI 컴포넌트 도입 PR에는 반드시 living catalog(dev 라우트)를 함께 작성한다.

**Why:** 카탈로그가 있으면 Claude가 신규 컴포넌트 상태를 브라우저에서 직접 확인할 수 있고, 사용자도 표준 reference로 활용 가능. Storybook 미설치이므로 Next.js dev 라우트 그룹 `(dev)` 사용.

**How to apply:** `front-end/src/app/(dev)/<name>-preview/page.tsx`를 함께 생성. variant × 상태 조합을 한 페이지에 나열.

---

## QA 에이전트 빌드·테스트 명령어 자동 허용

QA 에이전트가 실행하는 빌드·테스트 명령어는 허가 요청 없이 자동 진행한다.

**Why:** QA 흐름에서 불필요한 중단을 방지하기 위해 사용자가 명시 지시.

**How to apply:** 새 PC에서 작업 시작 시 `.claude/settings.json`의 `permissions.allow`에 다음 명령어 등록:
- `npm --prefix front-end run lint`
- `npm --prefix front-end run build`
- `npm --prefix front-end run test` / `npm --prefix front-end test *`
- `npx --prefix front-end tsc --noEmit`
- `back-end/java-spring/gradlew.bat -p back-end/java-spring test`
- `back-end/java-spring/gradlew.bat -p back-end/java-spring build`

신규 QA 명령어가 추가될 경우 `settings.json` `permissions.allow`에 함께 추가.

---

## CLAUDE.md 간결 유지 방침

CLAUDE.md는 최대한 짧게 유지한다. 파이프라인 동작에 직접 영향을 주지 않는 참조 정보는 추가하지 않는다.

**Why:** "CLAUDE.md는 최대한 짧게 유지해야 해" — 사용자 명시 지시.

**How to apply:** CLAUDE.md 수정 제안 시 동작에 필수적인 내용인지 먼저 판단. 문서화 목적만이라면 PIPELINE.md 등 다른 파일에 위임하거나 스킵.
