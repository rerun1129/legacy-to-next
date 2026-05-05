# 프론트엔드 코딩 규칙 (Next.js / React / TypeScript)

> **이 파일은 프론트엔드(Next.js · React · TypeScript) 도메인 코드 작업 규칙입니다.**
> 공용 섹션(A1~A6, ARCH1~5, CONV1~2, C1~4, C7~8)은 `backend_coding_rules.md`와 동일하게 복제됩니다.
> **공용 섹션 변경 시 두 파일을 반드시 동시에 수정하세요.**


# AI 코드 안티패턴

A는 AI 에이전트입니다. AI는 사람과 다른 실수를 **체계적으로 반복**합니다.

## A1. "조용한 실패" 금지

빈 catch, 무차별 null 반환, 빈 배열/기본값으로 대체, 에러를 그대로 무시한 옵셔널 체이닝

## A2. eslint-disable 로 회피 금지

`exhaustive-deps` 경고는 **신호**다. 의존성을 정확히 채우거나, 정확히
채울 수 없는 구조라면 `useCallback`/`useMemo`/ref 패턴으로 재설계.
주석으로 끄는 건 버그를 미래로 미루는 행위.

## A3. Happy path 만 구현 금지

## A4. 유사 함수 복붙 금지

3개 이상의 유사한 함수가 보이면 추상화 신호. AI는 사람보다 훨씬 자주 "비슷한데 살짝 다른" 함수를 양산한다.
단, 추상화가 강제로 결합도를 만들 정도라면 차라리 중복이 낫다. 판단은 신중히.

## A5. 루프 안 순차 await

순서가 중요하거나 rate limit 고려 필요하면 그대로 유지하되, 주석으로 이유를 남길 것.

## A6. 추측성 방어 코드 금지

TypeScript strict 모드와 코드 흐름상 절대 null 일 수 없는 곳에 `?.`나 `if (x !== null)` 이 있으면 위반.


# 아키텍처 규칙

## ARCH1. 헥사고널 아키텍처 레이어 경계

**적용 범위**: 도메인 코드(`domain/`, `application/`, `adapter/`)에만 적용.

### 의존 방향 (필수)

- **Adapter → Application → Domain** 만 허용
- **역방향 절대 금지** (Domain 이 Application 을 import → 위반)
- **Domain 은 외부 의존 0**: 프레임워크, HTTP 클라이언트, 파일 I/O, 환경변수, 시간(`Date.now()`, `new Date()`) 직접 사용 금지
- **Application 은 Port 인터페이스만 의존**, 어댑터 구현체를 직접 import 금지

## ARCH2. 파일 크기

500줄 초과 절대 금지.
예외:
- 자동 생성 파일 (OpenAPI 클라이언트, GraphQL 코드젠 등)
- 단순 매핑/상수 테이블 (`enum`, `constants.ts`, lookup 테이블)
- 테스트 파일 (테스트 케이스 다수가 한 파일에 모이는 건 자연스러움)

## ARCH3. 사이드 이펙트 분리

순수 계산과 I/O 는 같은 함수에 섞지 않는다. 도메인 엔티티 메서드는 항상 순수해야 한다.

## ARCH4. 입력/출력 경계 명시

Adapter(in)에서 외부 형식(API 응답, URL param, form) → 도메인 변환,
Adapter(out)에서 도메인 → 외부 형식 변환.

## ARCH5. 시간/난수/UUID 의 추상화

`Date.now()`, `Math.random()`, `crypto.randomUUID()` 직접 호출 금지.
Clock/IdGenerator 포트로 추상화. React 컴포넌트에서는 props 또는 Context 주입.

## ARCH6. [Critical] 프론트엔드 설계 시 예외

프론트엔드는 Next.js 라우팅 제약으로 `app/`·`components/` 위치를 유지하며, `domain/` · `application/` · `adapter/out/` 레이어 분리로 의존성
방향(adapter → application → domain)을 준수하는 것으로 헥사고널 아키텍처 적용 기준을 충족한 것으로 간주한다.


# 코딩 컨벤션

## CONV1. 케이스

- 타입/클래스/React 컴포넌트: PascalCase
- 함수/변수: camelCase
- 모듈 레벨 불변 상수: UPPER_SNAKE_CASE (zod 스키마 포함)

## CONV2. 주석

- "무엇" 이 아닌 "왜". 코드를 읽으면 알 수 있는 내용 반복 금지.
- 정당화 가능한 주석:
    - 의도/제약/비즈니스 규칙 출처
    - 비자명한 알고리즘 선택 근거
    - 의도된 fire-and-forget, 의도된 빈 분기 등


# 정확성 규칙

코드의 **동작 정확성**과 **운영 안정성**에 관한 규칙.

## C1. 에러 처리

- 에러를 `string` 으로 throw 금지. `Error` 인스턴스 사용.
- 에러 객체에 컨텍스트 포함. 단순 `throw e` 보다 `throw new XxxError(msg, { cause: e })`.
- 도메인 에러는 별도 클래스로. `ValidationError`, `NotFoundError` 등. 무차별 `Error` 만 쓰면 호출자가 분기 불가.
- 에러 메시지에 식별자 포함.

## C2. 비동기 코드

- `async` 함수의 반환을 그대로 두면 안 됨. await 또는 명시적 fire-and-forget 주석.
- `async` 함수에서 `.then().catch()` 와 `await` 를 섞지 않는다.
- Promise rejection 미처리(`unhandledRejection`)가 발생할 수 있는 코드 경로 금지.
- 동시성 제어가 필요한 곳에서 단순 `Promise.all` 만 쓰면 외부 시스템 과부하. rate limit / batch / 세마포어 검토.
- fetch 요청은 중복 실행 방지를 위해 `AbortController` 활용 고려.

## C3. 입력 검증 (도메인 경계)

- URL param, query string, form, 외부 API 응답은 사용 전 검증.
- 검증 실패 시 도메인 에러로 변환하여 throw.
- 검증은 어댑터 진입 직후. Use Case 안에서 다시 검증할 필요 없음.
- zod 스키마 라이브러리 우선 사용.

## C4. 자원 해제

타이머(`setTimeout`/`setInterval`), 이벤트 리스너, 네트워크 소켓, 구독(subscription)은
`useEffect` cleanup 또는 try/finally 로 해제.

## C6. React 정확성

- `useEffect` 의존성 배열은 정확해야 함. 누락 시 stale closure 버그.
- `useMemo`/`useCallback` 은 비용/이득을 따져 선택적 사용. 무조건 감싸지 말 것.
- 상태 업데이트 함수에 객체를 직접 mutate 하고 setState 호출 금지 — 새 객체를 만들어야 React 가 리렌더 인식.
- Effect 안에서 비동기 함수 직접 정의 후 await 호출 금지. cleanup 처리에 주의.

## C7. 동시성 안전성

- 동시 요청 race condition — 최신 요청 결과만 반영하도록 flag/AbortController/ref 패턴 사용.
- stale state 업데이트: 언마운트 후 setState 호출 방지 (cleanup으로 처리).
- 외부 API 호출은 멱등성(idempotency) 고려. 재시도 시 중복 실행 안전한지.

## C8. 로그 위생

- 비밀번호, 토큰, API 키, PII 출력 금지.
- 브라우저 콘솔에 토큰·민감 정보 노출 금지.
- 운영 배포에 `console.log` 디버그 흔적 커밋 금지.
- 에러 로그는 식별자 + 컨텍스트 포함.
