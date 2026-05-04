# legacy-to-next

> 화물 포워딩 시스템을 **헥사고널 + ArchUnit**으로 재구축하는 1인 풀스택 포트폴리오.

본 프로젝트는 세 가지 가설을 1인 환경에서 실증하는 것을 명시적 목표로 합니다 — (1) 헥사고널 아키텍처와 ArchUnit이 *소규모 프로젝트에서도* 의존 강제 도구로 작동하는가, (2) RFC 7807 + Zod 4로 *양 스택 타입 안전성*을 어디까지 끌어올릴 수 있는가, (3) Claude Code 기반 설계→구현→검토 사이클의 *리드타임 효과*는 어떠한가.

**Stack** — Java 21 · Spring Boot 3.4 · Next.js 16 · React 19 · PostgreSQL 17 · Claude Code

---

## Status

| 모듈 | 영역 | 상태 |
|---|---|---|
| **FMS** | 화물 관리 — B/L · EDI · D/O · 컨테이너 | ✅ 구현 완료 (Phase 1) |
| **BMS** | 정산 — 운임 계산 · 청구 | 🗓 예정 (Phase 2) |
| **PFM** | 실적 — 집계 · 분석 · 대시보드 | 🗓 예정 (Phase 3) |
| **Admin** | 권한 · 코드 테이블 관리 | 🗓 예정 (Phase 4) |

**Phase 1 (FMS) 완료 기준** — 3개 서브도메인(Master B/L · House B/L · Switch B/L) · 24개 도메인 엔티티 · 20개 테이블 · 약 453개 백엔드 테스트 케이스 · Vitest 65 + Playwright E2E 4 spec · 헥사고널 4계층 ArchUnit 강제 · OpenAPI 3.x · RFC 7807 · Prometheus 메트릭.

---

## Architecture

### 백엔드 — 헥사고널 4계층 (ArchUnit 강제)

```
domain                    POJO 엔티티 · Port · VO · Enum  (Spring/JPA 의존 없음)
  ↓
application               UseCase 구현 (@Service)
  ↓
adapter/in/web            Controller · Assembler · DTO
adapter/out/persistence   JpaEntity · QueryDSL · Mapper
```

ArchUnit 1.3 규칙으로 빌드 단계에서 의존 방향 위반을 차단합니다. `domain`은 `adapter`/`application`/Spring을 참조할 수 없고, `@Entity`는 `..adapter.out.persistence..` 패키지 전용입니다.

→ [`HexagonalArchitectureTest.java`](back-end/java-spring/src/test/java/com/freightos/fms/architecture/HexagonalArchitectureTest.java)

### 프론트엔드 — 헥사고널 3계층 + bindings.ts 어댑터 스왑

```
domain        순수 타입 · 엔티티
  ↓
application   포트 인터페이스 · 유즈케이스 팩토리 · bindings.ts
  ↓
adapter/out   API 구현  /  Mock 구현
```

`bindings.ts` 한 파일 교체로 API ↔ Mock 어댑터를 스왑합니다. 백엔드 미구현 상태에서도 FE 독립 개발이 가능하며, MSW 없이 Mock 어댑터 자체가 테스트 더블 역할을 합니다.

→ [`house-bl/bindings.ts`](front-end/src/application/house-bl/bindings.ts)

### 양 스택 공통 표준

OpenAPI 3.x · RFC 7807 problem+json · Zod 4 런타임 응답 검증 · JSON 구조화 로깅 (`trace_id`/`span_id`/`service`) · Prometheus 메트릭

---

## Tech Stack

| 영역 | 핵심 |
|---|---|
| **Backend** | Java 21 · Spring Boot 3.4 · Spring Data JPA · QueryDSL 5.1 · PostgreSQL 17 · Resilience4j 2.3 · springdoc-openapi |
| **Frontend** | Next.js 16 App Router · React 19 · TypeScript strict · Zustand 5 · TanStack Query/Table/Virtual · Zod 4 · Tailwind v4 |
| **Testing** | JUnit 5 + Mockito · ArchUnit 1.3 · H2 슬라이스(@WebMvcTest/@DataJpaTest) · Vitest 2 · Playwright 1.59 |
| **Planned** | Spring Security + JWT · Spring Batch 5 · Flyway 활성화 · Python 3.13 + FastAPI (Phase 2) · AWS 인프라 · MongoDB Analytics Mart (PFM) |

<details>
<summary>버전·의존성 전체 목록</summary>

전체 백엔드 의존성: [`back-end/java-spring/build.gradle`](back-end/java-spring/build.gradle)
전체 프론트엔드 의존성: [`front-end/package.json`](front-end/package.json)

</details>

---

## Quickstart

```bash
# 사전 요구: Java 21+, Node.js 20+, PostgreSQL 17 (port 5432)

# Backend
cd back-end/java-spring && ./gradlew bootRun
# → http://localhost:8080/swagger-ui.html

# Frontend
npm --prefix front-end run dev
# → http://localhost:3000  ·  Living Catalog: /preview
```

테스트 — 백엔드 `./gradlew test` · 프론트엔드 `npm --prefix front-end run test`.

---

## Deep Dive

| 문서 | 내용 |
|---|---|
| [`docs/portfolio/CAREER_REFERENCE.md`](docs/portfolio/CAREER_REFERENCE.md) | 경력 기술서 1차 자료 — 프로젝트 수치·강점·JD 매핑 |
| [`docs/fms/00_OVERVIEW...md`](docs/fms/00_OVERVIEW_20260423_210000.md) | FMS PRD — 개요 |
| [`docs/fms/01_DOMAIN...md`](docs/fms/01_DOMAIN_20260424_174104.md) | FMS PRD — 도메인 모델 (E-01 ~ E-24) |
| [`docs/fms/02_SCREENS...md`](docs/fms/02_SCREENS_20260424_174104.md) | FMS PRD — 화면 설계 |
| [`docs/fms/03_TECH_STACK...md`](docs/fms/03_TECH_STACK_20260424_174104.md) | FMS PRD — 기술 스택 |
| [`docs/fms/05_NON_FUNCTIONAL...md`](docs/fms/05_NON_FUNCTIONAL_20260424_161404.md) | FMS PRD — 비기능 요구사항 (SLA · 성능 KPI · 보안) |
| [`CLAUDE.md`](CLAUDE.md) | Claude Code 협업 규칙 · 빌드 명령어 |
| [`.claude/agents/PIPELINE.md`](.claude/agents/PIPELINE.md) | 서브 에이전트 파이프라인 정의 |
| [`rules/master_coding_rules.md`](rules/master_coding_rules.md) | 코딩 룰 SSOT |

<details>
<summary>디렉토리 구조</summary>

```
legacy-to-next/
├── back-end/java-spring/     Spring Boot 3.4 백엔드 — 헥사고널 4계층
├── front-end/                Next.js 16 프론트엔드 — 헥사고널 3계층
├── schema/                   PostgreSQL DDL (FMS 20테이블)
├── docs/fms/                 FMS PRD 문서군
├── docs/portfolio/           포트폴리오 · 경력 자료
├── rules/                    코딩 룰 SSOT
├── .claude/                  Claude Code 협업 정의
└── CLAUDE.md                 Claude Code 진입점
```

</details>
