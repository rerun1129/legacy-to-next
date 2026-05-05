# 남은 작업 — 헥사고널 아키텍처 계층 분리 완성

> 작성일: 2026-05-06  
> 컨텍스트 클리어 후에도 이 파일만 보고 작업을 이어갈 수 있도록 배경·패턴·세부 명세를 모두 담았다.

---

## 1. 배경 — 이번 세션에서 무엇을 했는가

`rules/backend_coding_rules.md` ARCH1에 두 가지 규칙이 새로 추가됐다:

1. **Port 위치**: `application/{도메인}/port/in|out/`에 둔다 (domain 레이어 port 디렉토리 금지)  
2. **Adapter(in) → Domain 직접 참조 금지**: Controller·Assembler는 Application Command/UseCase만 알아야 한다. DTO→Command 변환은 Assembler, Command→Entity/VO 변환은 Application Factory 책임.

이 규칙에 따라 **housebl, switchbl, masterbl** 세 도메인에 아래 패턴을 적용 완료했다.

```
[완성된 패턴]

Adapter(in)                Application               Domain
──────────────────         ──────────────────         ──────────────
Controller                 UseCase (port/in)           Entity / VO
  ↓ DTO                      ↓ Command                  (HouseBl 등)
Assembler                  Factory
  DTO → Command               Command → Entity/VO
  Result → Response DTO       Entity → DetailResult
                           Service
                             @Transactional
                             Factory + Port 위임
                           Port (port/out)
                           DetailResult (projection/)
```

**housebl 기준 완성 파일 목록 (참고용)**:
- `application/housebl/command/CreateHouseBlCommand.java`
- `application/housebl/command/UpdateHouseBlCommand.java`
- `application/housebl/projection/HouseBlDetailResult.java`
- `application/housebl/HouseBlFactory.java` ← Command→Entity/VO, Entity→DetailResult
- `adapter/in/web/housebl/HouseBlAssembler.java` ← domain import 0개
- `adapter/in/web/housebl/dto/HouseBlDetailResponse.java` ← `from(HouseBlDetailResult)`

---

## 2. 남은 작업 목록

### Task 1. HouseBlFactory 분리 (300줄 초과 → 500줄 강제 분리 전 예방 조치)

**현재 상태**: `HouseBlFactory.java` 422줄 (300줄 검토 임계치 초과)

**원인**: Create/Update × sub 엔티티 6종(Desc/Dim/Container/ScheduleLeg/TruckOrder/AirCharge) 변환 로직이 한 클래스에 집중.

**수행 작업**:
1. `application/housebl/HouseBlSubFactory.java` 신규 생성
   - sub 엔티티 관련 메서드 이전: `applyDescCreate`, `applyDimCreate`, `applyContainerCreate`, `applyScheduleLegCreate`, `applyTruckOrderCreate`, `applyAirChargeCreate` (Update 대응도 포함)
2. `HouseBlFactory`에서 `HouseBlSubFactory`를 주입받아 위임
   - `@Component`, `HouseBlSubFactory sub` 필드 추가
3. 목표: `HouseBlFactory` 230줄 이하, `HouseBlSubFactory` 230줄 이하

**영향 파일**: `HouseBlFactory.java` 수정, `HouseBlSubFactory.java` 신규  
**테스트**: 기존 테스트가 Factory를 직접 호출하지 않으면 변경 불필요 (Service 통해 간접 테스트)

---

### Task 2. TruckBl/NonBl — Domain Projection → Application Projection 이전

**현재 상태**:
- `TruckBlAssembler.java` (13줄): `domain.truckbl.projection.TruckBlSummary` 참조
- `NonBlAssembler.java` (12줄): `domain.nonbl.projection.NonBlSummary` 참조

**문제**: Projection이 `domain.*/projection/`에 있어서 Assembler(Adapter)가 domain을 직접 참조.

**수행 작업**:
1. `application/truckbl/projection/TruckBlSummary.java` 신규 생성 (기존 `domain/truckbl/projection/TruckBlSummary.java` 내용 그대로 복사, package만 변경)
2. `application/nonbl/projection/NonBlSummary.java` 신규 생성 (동일 패턴)
3. `domain/truckbl/projection/TruckBlSummary.java` 삭제
4. `domain/nonbl/projection/NonBlSummary.java` 삭제
5. 모든 import 경로 갱신:
   - `com.freightos.fms.domain.truckbl.projection.TruckBlSummary` → `com.freightos.fms.application.truckbl.projection.TruckBlSummary`
   - `com.freightos.fms.domain.nonbl.projection.NonBlSummary` → `com.freightos.fms.application.nonbl.projection.NonBlSummary`
6. **영향 파일** (grep으로 확인): TruckBlSearchPort, TruckBlSearchService, TruckBlPersistenceAdapter, TruckBlAssembler, TruckBlSummaryResponse.from(), NonBl 동일

---

### Task 3. HouseBl/MasterBl Summary Projection → Application 이전

**현재 상태**:
- `domain/housebl/projection/HouseBlSummary.java` — UseCase.searchHouseBls가 `PagedResult<HouseBlSummary>`(domain) 반환
- `domain/masterbl/projection/MasterBlSummary.java` — 동일

**문제**: Task 2와 동일. UseCase → Assembler 경로에서 domain projection 참조.

**수행 작업** (Task 2 패턴과 동일):
1. `application/housebl/projection/HouseBlSummary.java` 신규 생성 후 기존 삭제
2. `application/masterbl/projection/MasterBlSummary.java` 신규 생성 후 기존 삭제
3. import 경로 갱신 (grep 후 일괄 치환)
   - `com.freightos.fms.domain.housebl.projection.HouseBlSummary` → `application.*`
   - `com.freightos.fms.domain.masterbl.projection.MasterBlSummary` → `application.*`

---

### Task 4. HouseBlFilter / SearchFilter → Application SearchCommand 도입

**현재 상태**:
- `HouseBlAssembler.toFilter(SearchHouseBlRequest)` 메서드가 `HouseBlFilter`(domain)를 직접 생성
- `TruckBlAssembler.toSummaryPage`는 검색 Command 없이 UseCase에 Filter 직접 전달
- `NonBlAssembler` 동일

**문제**: Assembler가 `domain.housebl.HouseBlFilter`, `domain.truckbl.TruckBlFilter` 등을 import해 ARCH1 위반.

**수행 작업**:
1. `application/housebl/command/SearchHouseBlCommand.java` 신규 (SearchHouseBlRequest 필드 그대로 primitive record)
2. `HouseBlAssembler.toFilter` → `toSearchCommand(SearchHouseBlRequest): SearchHouseBlCommand` 로 변경 (domain import 제거)
3. `HouseBlFactory.toFilter(SearchHouseBlCommand): HouseBlFilter` 신규 — Application이 Command→domain Filter 변환 책임
4. `HouseBlUseCase.searchHouseBls(HouseBlFilter, PageRequest)` → `searchHouseBls(SearchHouseBlCommand, PageRequest)` (또는 Service가 변환 처리)
5. TruckBl/NonBl 동일 패턴 적용

---

### Task 5. Enums — EnumOption Domain 참조 처리

**현재 상태**:
- `EnumAssembler.java` (28줄): `domain.enums.EnumOption` 참조
- `toResponse(List<EnumOption>)` 메서드에서 domain 타입 직접 사용

**문제**: EnumOption이 `domain.enums`에 있어서 Assembler가 domain 참조.

**수행 작업 옵션** (사용자 결정 필요):
- **A**: `EnumOption`을 `application/enums/projection/EnumOption.java`으로 이전
- **B**: EnumOption이 순수 VO 성격이면 `domain/common/`에 두고 공통 패키지로 간주 (예외 허용)

---

## 3. 작업 순서 권장

```
Task 2 (TruckBl/NonBl Projection 이전)  ← 단순, 영향 범위 작음
  ↓
Task 3 (HouseBl/MasterBl Summary 이전)  ← Task 2와 동일 패턴
  ↓
Task 1 (HouseBlFactory 분리)            ← 독립 작업, 언제든 가능
  ↓
Task 4 (SearchFilter → SearchCommand)   ← 가장 복잡, 선행 Task 완료 후
  ↓
Task 5 (Enums 처리)                     ← 사용자 결정 후 진행
```

---

## 4. 작업 시 참조해야 할 파일

| 역할 | 경로 |
|---|---|
| 완성된 Command 패턴 참조 | `application/housebl/command/CreateHouseBlCommand.java` |
| 완성된 Factory 참조 | `application/housebl/HouseBlFactory.java` |
| 완성된 Projection 참조 | `application/housebl/projection/HouseBlDetailResult.java` |
| 완성된 Assembler 참조 | `adapter/in/web/housebl/HouseBlAssembler.java` |
| ARCH 규칙 | `rules/backend_coding_rules.md` ARCH1 |
| 빌드 명령 | `back-end/java-spring/gradlew.bat -p back-end/java-spring build` |
| 테스트 명령 | `back-end/java-spring/gradlew.bat -p back-end/java-spring test` |

---

## 5. 에이전트 실행 방법

모든 작업은 Backend-coder 에이전트에게 위임한다.  
QA 에이전트로 빌드/테스트 검증 후 완료 처리.

빌드 FAIL 시 `tsc --noEmit`은 프론트엔드용이므로 백엔드는  
`gradlew.bat -p back-end/java-spring compileTestJava`로 전체 컴파일 오류 수집.
