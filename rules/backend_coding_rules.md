# 백엔드 코딩 규칙 (Spring Boot / Java)

> **이 파일은 백엔드(Spring Boot · Java) 도메인 코드 작업 규칙입니다.**
> 공용 섹션(A1~A6, ARCH1~5, CONV1~2, C1~4, C7~8)은 `frontend_coding_rules.md`와 동일하게 복제됩니다.
> **공용 섹션 변경 시 두 파일을 반드시 동시에 수정하세요.**


# AI 코드 안티패턴

A는 AI 에이전트입니다. AI는 사람과 다른 실수를 **체계적으로 반복**합니다.

## A1. "조용한 실패" 금지

빈 catch, 무차별 null 반환, 빈 배열/기본값으로 대체, 에러를 그대로 무시한 옵셔널 체이닝

## A2. 정적 분석기 경고 회피 금지

`@SuppressWarnings`, Checkstyle/SpotBugs/Sonar suppress 주석으로 경고를 끄지 말 것.
경고는 **신호**다. 의존성을 정확히 채우거나, 채울 수 없는 구조라면 근본 원인을 해결.
주석으로 끄는 건 버그를 미래로 미루는 행위.

## A3. Happy path 만 구현 금지

## A4. 유사 함수 복붙 금지

3개 이상의 유사한 함수가 보이면 추상화 신호. AI는 사람보다 훨씬 자주 "비슷한데 살짝 다른" 함수를 양산한다.
단, 추상화가 강제로 결합도를 만들 정도라면 차라리 중복이 낫다. 판단은 신중히.

## A5. 루프 안 순차 블로킹 호출

순서가 중요하거나 rate limit 고려 필요하면 그대로 유지하되, 주석으로 이유를 남길 것.
병렬화 가능한 경우 `CompletableFuture.allOf` 또는 Reactor `Flux.flatMap(concurrency)` 검토.

## A6. 추측성 방어 코드 금지

`@NonNull` 보장된 곳의 불필요한 null check, `Optional.ofNullable` 남용 금지.
타입 시스템과 코드 흐름상 절대 null 일 수 없는 곳에 방어 코드가 있으면 위반.


# 아키텍처 규칙

## ARCH1. 헥사고널 아키텍처 레이어 경계

**적용 범위**: 도메인 코드(`domain/`, `application/`, `adapter/`)에만 적용.

### 의존 방향 (필수)

- **Adapter → Application → Domain** 만 허용
- **역방향 절대 금지** (Domain 이 Application 을 import → 위반)
- **Domain 은 외부 의존 0**: Spring 프레임워크, JPA, RestTemplate, HTTP 클라이언트, 파일 I/O, 환경변수, 시간(`Instant.now()`, `LocalDateTime.now()`) 직접 사용 금지
- **Application 은 Port 인터페이스만 의존**, 어댑터 구현체를 직접 import 금지
- **Port 인터페이스는 `application/{도메인}/port/in|out/`에 둔다.** Domain 레이어는 Entity/VO/도메인 서비스만 보유하며 port 디렉토리를 두지 않는다.
- **Adapter(in)은 Domain 레이어를 직접 참조하지 않는다.** Presentation 계층(Controller, Assembler)은 Application Command/UseCase만 알아야 한다. DTO → Command 변환은 Assembler, Command → 도메인 Entity/VO 변환은 Application Factory/Service 책임이다. 계층 건너뛰기(Adapter(in) → Domain 직접 호출) 절대 금지.

## ARCH2. 파일 크기

500줄 초과 절대 금지.
예외:
- 자동 생성 파일 (Flyway 마이그레이션, MapStruct, OpenAPI generator 등)
- 단순 매핑/상수 테이블 (`enum`, constants 클래스, lookup 테이블)
- 테스트 파일 (테스트 케이스 다수가 한 파일에 모이는 건 자연스러움)

## ARCH3. 사이드 이펙트 분리

순수 계산과 I/O 는 같은 함수에 섞지 않는다. 도메인 엔티티 메서드는 항상 순수해야 한다.

## ARCH4. 입력/출력 경계 명시

Adapter(in)에서 외부 형식(Request DTO) → 도메인 변환,
Adapter(out)에서 도메인 → 외부 형식(JPA Entity, Response DTO) 변환.

## ARCH5. 시간/난수/UUID 의 추상화

`Instant.now()`, `LocalDateTime.now()`, `UUID.randomUUID()`, `Math.random()`, `ThreadLocalRandom` 직접 호출 금지.
`java.time.Clock` 빈 주입 또는 `IdGenerator` 포트 인터페이스로 추상화.

(ARCH6은 프론트엔드 전용 규칙으로 이 파일에 해당 없음)


# 코딩 컨벤션

## CONV1. 케이스

- 타입/클래스: PascalCase
- 메서드/변수: camelCase
- `static final` 상수: UPPER_SNAKE_CASE

## CONV2. 주석

- "무엇" 이 아닌 "왜". 코드를 읽으면 알 수 있는 내용 반복 금지.
- 정당화 가능한 주석:
    - 의도/제약/비즈니스 규칙 출처
    - 비자명한 알고리즘 선택 근거
    - 의도된 fire-and-forget, 의도된 빈 catch (`// intentionally ignored: <이유>`)


# 정확성 규칙

코드의 **동작 정확성**과 **운영 안정성**에 관한 규칙.

## C1. 에러 처리

- 예외를 `String` 으로 throw 금지. `Exception` 계층 인스턴스 사용.
- 예외 객체에 컨텍스트 포함. 단순 `throw e` 보다 `throw new XxxException(msg, cause)`.
- 도메인 예외는 별도 클래스로. `ValidationException`, `NotFoundException` 등. 무차별 `RuntimeException` 만 쓰면 호출자가 분기 불가.
- 예외 메시지에 식별자 포함.

## C2. 비동기 코드

- `CompletableFuture` 반환값 무시 금지. `.join()`/`.get()` 누락 시 silent loss.
- Reactor `Mono`/`Flux` 미구독(subscribe 없음) 금지.
- `@Async` 메서드 반환 타입 주의 — `Future`/`CompletableFuture` 반환 시 결과 처리 책임 명시.
- 동시성 제어가 필요한 곳에서 단순 병렬 호출만 쓰면 외부 시스템 과부하. rate limit / batch / 세마포어 검토.

## C3. 입력 검증 (도메인 경계)

- HTTP body, query param, URL param, 외부 API 응답은 사용 전 검증.
- 검증 실패 시 도메인 예외로 변환하여 throw.
- 검증은 어댑터 진입 직후 (`@Valid` + JSR-380, 또는 도메인 객체 생성자 검증). Use Case 안에서 다시 검증할 필요 없음.

## C4. 자원 해제

파일 핸들, DB 커넥션/트랜잭션, 네트워크 소켓은 try-with-resources 또는 `@Transactional` 경계로 해제.
JDBC `Connection`, `InputStream`, Reactor 구독은 반드시 닫거나 dispose 처리.

## C5. SQL 정확성

- N+1 쿼리 패턴 금지. JPA 사용 시 명시적 `@EntityGraph`/`fetch join`/`JPQL join fetch`.
- LEFT JOIN + WHERE 우측 조건으로 INNER JOIN 효과를 내는 패턴 금지.
- 대용량 테이블 OFFSET 페이징 금지. cursor 기반 페이징 사용.

(C6은 React 전용 규칙으로 이 파일에 해당 없음)

## C7. 동시성 안전성

- 공유 가변 상태에 대한 동시 접근 명시적 처리 (락, 큐, 메시지 패싱, 분산 락).
- DB 트랜잭션 격리 수준이 의도와 맞는지 확인 (`SELECT FOR UPDATE` 누락, read-committed 가정 위반 등).
- 외부 시스템 호출은 멱등성(idempotency) 고려. 재시도 시 중복 실행 안전한지 (Idempotency-Key, Outbox 패턴 등).

## C8. 로그 위생

- 비밀번호, 토큰, API 키, PII 출력 금지.
- `System.out.println` 대신 SLF4J 사용. 운영 로그에 디버그 흔적 커밋 금지.
- 에러 로그는 stack trace + 식별자 + 컨텍스트 포함. MDC(Mapped Diagnostic Context) 트레이싱 활용.
- 구조화 로그 사용 권장. string concat 보다 메타데이터 필드.


# 프로젝트 누적 규칙

사용자 검수 라운드에서 도출된 이 프로젝트 특화 규칙.

## P1. Adapter(in) 계층

- Controller는 응답 데이터의 매핑·변환을 직접 호출하지 않는다 (`.map(Dto::from)`, `Dto.from(domain)` 금지). 도메인 → DTO 변환은 별도 어셈블러/매퍼 컴포넌트에 위임하고, 컨트롤러는 어셈블러 메서드 한 번 호출로 끝낸다.
- URL 경로에 버전을 포함하지 않는다 (`/api/v1/...` 금지). 버저닝이 필요하면 헤더로 처리한다.

## P2. Adapter(out) 계층

- Adapter는 도메인 타입 ↔ 인프라 타입 변환을 담당한다. 도메인 PageRequest → Spring Pageable 변환, 엔티티 매핑 등 인프라 특화 처리는 어댑터 책임이다.
- 비즈니스 정책 결정(정렬 기준·방향, 필터 조건 등)은 애플리케이션 계층(서비스)이 도메인 개념으로 결정하고 포트에 전달한다. 어댑터는 그 결정을 인프라 타입(Spring Sort 등)으로 변환할 뿐이다.
- 애플리케이션 계층은 JPA·Spring 등 인프라 세부사항을 알 필요가 없다.

## P3. 명명

- 메서드명은 동작 + 대상 + 조건으로 구성한다. 같은 클래스·인터페이스 내에서도 대상을 생략하지 않는다.
  - ❌ `getById`, `findAll`, `deleteById`, `list`, `search`, `process`, `handle`
  - ✅ `getHouseBlById`, `getHouseBlsByJobDivAndBound`, `deleteHouseBlById`
- 정렬 기준·방향은 메서드명에 포함하지 않는다(`OrderByCreatedAtDesc` 등 금지). 정렬은 `PageRequest` 등 파라미터로 전달한다.
- 조회 메서드 접두어: `find`는 0..1 (없을 수 있음), `get`은 1..n (반드시 있거나 여러 개).

## P4. 가독성

- 1회만 참조되는 지역 변수는 인라인한다.
- if-else 분기가 3개 이상 체이닝되면 switch 문(또는 switch 표현식)으로 교체한다. default(예외 처리 포함)도 분기 수에 포함한다.
- pattern matching switch에서 패턴 변수를 사용하지 않을 경우 `ignored`로 명명한다(`case Type ignored ->`).
- 여러 줄로 분리된 식(메서드 체이닝, 삼항 연산자, 파라미터 목록 등)은 합쳤을 때 **180열 이하**이면 한 줄로 작성한다. 180열을 초과할 때만 개행한다.
- 도메인 엔티티의 update/assign 메서드는 **파라미터 8개 이상일 때** 엔티티 내부 `public static record XxxFields` 로 그룹화한다. 5~7개는 의미 단위가 응집되어 있으면 그대로 둔다. record 명명은 `XxxFields` 접미사로 통일.

## P5. 응답 메시지

- 사용자 노출 문자열을 코드에 하드코딩하지 않는다. 메시지 코드(`enum`) + `MessageSource` 기반 i18n으로 처리한다.

## P6. 도메인 모델

- 도메인 값이 (a) 자기 검증·연산이 있거나 (b) 동일 의미 primitive가 2곳 이상이면 `domain/common/vo/`에 record VO로 정의한다. 검증은 compact constructor 또는 `static of(...)` 팩토리에서 수행하며, null/blank 입력은 null 반환으로 처리한다.
- VO에는 jakarta·JPA 어노테이션을 두지 않는다. `@Embeddable` 금지. JPA 매핑은 어댑터 매퍼에서 `VO.of(jpa.getString())` / `vo != null ? vo.value() : null`로 처리하고, JpaEntity는 인프라 타입(String/BigDecimal)을 유지한다.
- 외부 API DTO는 인터페이스 호환을 위해 primitive(String 등)를 유지한다. 도메인 진입 시 `VO.of(...)`, 도메인 이탈 시 `vo.asString()` / `vo.value()`로 변환한다.
- 어댑터 매퍼에서 VO → JPA 타입 null-safe 변환은 삼항 연산자 대신 `VoMapper.mapOrNull(vo, VO::method)`를 사용한다 (`adapter/out/persistence/common/VoMapper.java`).

## P7. 테스트

- Mockito BDD 스타일에서 인터페이스 메서드명을 변경할 때 `given(port.method(` 패턴과 `then(port).should().method(` 패턴을 모두 교체한다. 한쪽만 바꾸면 컴파일 오류가 발생한다.
