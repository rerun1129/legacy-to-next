# Master Coding Rules

사용자 검수 라운드에서 도출된 코딩 규칙을 누적 기록한다.

## Adapter(in) 계층
- Controller는 응답 데이터의 매핑·변환을 직접 호출하지 않는다 (`.map(Dto::from)`, `Dto.from(domain)` 금지). 도메인 → DTO 변환은 별도 어셈블러/매퍼 컴포넌트에 위임하고, 컨트롤러는 어셈블러 메서드 한 번 호출로 끝낸다.
- URL 경로에 버전을 포함하지 않는다 (`/api/v1/...` 금지). 버저닝이 필요하면 헤더로 처리한다.

## Adapter(out) 계층
- Adapter는 도메인 타입 ↔ 인프라 타입 변환을 담당한다. 도메인 PageRequest → Spring Pageable 변환, 엔티티 매핑 등 인프라 특화 처리는 어댑터 책임이다.
- 비즈니스 정책 결정(정렬 기준·방향, 필터 조건 등)은 어플리케이션 계층(서비스)이 도메인 개념으로 결정하고 포트에 전달한다. 어댑터는 그 결정을 인프라 타입(Spring Sort 등)으로 변환할 뿐이다.
- 어플리케이션 계층은 JPA·Spring 등 인프라 세부사항을 알 필요가 없다.

## 명명
- 메서드명은 동작 + 대상 + 조건으로 구성한다. 같은 클래스·인터페이스 내에서도 대상을 생략하지 않는다.
  - ❌ `getById`, `findAll`, `deleteById`, `list`, `search`, `process`, `handle`
  - ✅ `getHouseBlById`, `getHouseBlsByJobDivAndBound`, `deleteHouseBlById`
- 정렬 기준·방향은 메서드명에 포함하지 않는다(`OrderByCreatedAtDesc` 등 금지). 정렬은 `PageRequest` 등 파라미터로 전달한다.
- 조회 메서드 접두어: `find`는 0..1 (없을 수 있음), `get`은 1..n (반드시 있거나 여러 개).

## 가독성
- 1회만 참조되는 지역 변수는 인라인한다.
- if-else 분기가 3개 이상 체이닝되면 switch 문(또는 switch 표현식)으로 교체한다. default(예외 처리 포함)도 분기 수에 포함한다.
- pattern matching switch에서 패턴 변수를 사용하지 않을 경우 `ignored`로 명명한다(`case Type ignored ->`).
- 여러 줄로 분리된 식(메서드 체이닝, 삼항 연산자, 파라미터 목록 등)은 합쳤을 때 **180열 이하**이면 한 줄로 작성한다. 180열을 초과할 때만 개행한다.
- 도메인 엔티티의 update/assign 메서드는 **파라미터 8개 이상일 때** 엔티티 내부 `public static record XxxFields` 로 그룹화한다. 5~7개는 의미 단위가 응집되어 있으면 그대로 둔다. record 명명은 `XxxFields` 접미사로 통일.

## 응답 메시지
- 사용자 노출 문자열을 코드에 하드코딩하지 않는다. 메시지 코드(`enum`) + `MessageSource` 기반 i18n으로 처리한다.

## 도메인 모델
- 도메인 값이 (a) 자기 검증·연산이 있거나 (b) 동일 의미 primitive가 2곳 이상이면 `domain/common/vo/`에 record VO로 정의한다. 검증은 compact constructor 또는 `static of(...)` 팩토리에서 수행하며, null/blank 입력은 null 반환으로 처리한다.
- VO에는 jakarta·JPA 어노테이션을 두지 않는다. `@Embeddable` 금지. JPA 매핑은 어댑터 매퍼에서 `VO.of(jpa.getString())` / `vo != null ? vo.value() : null`로 처리하고, JpaEntity는 인프라 타입(String/BigDecimal)을 유지한다.
- 외부 API DTO는 인터페이스 호환을 위해 primitive(String 등)를 유지한다. 도메인 진입 시 `VO.of(...)`, 도메인 이탈 시 `vo.asString()` / `vo.value()`로 변환한다.
- 어댑터 매퍼에서 VO → JPA 타입 null-safe 변환은 삼항 연산자 대신 `VoMapper.mapOrNull(vo, VO::method)`를 사용한다 (`adapter/out/persistence/common/VoMapper.java`).

## 테스트
- Mockito BDD 스타일에서 인터페이스 메서드명을 변경할 때 `given(port.method(` 패턴과 `then(port).should().method(` 패턴을 모두 교체한다. 한쪽만 바꾸면 컴파일 오류가 발생한다.