# Legacy-to-Next — 화물 포워딩 시스템 재구축 포트폴리오

> Java/Spring · Next.js 듀얼 스택으로 화물 포워딩 시스템(업무·정산·실적)을 헥사고널 아키텍처와 ArchUnit으로 재구축하는 풀스택 포트폴리오 프로젝트

---

## 프로젝트 소개

포워딩(국제 화물 운송 중개) 레거시 시스템을 재구축하는 포트폴리오 프로젝트로, 신규 고객사 프로덕션 투입을 전제로 한 품질 수준을 목표로 설계하였습니다.

Java 21 + Spring Boot 기반 백엔드를 헥사고널 아키텍처로 설계하고, ArchUnit으로 4계층 의존 방향을 빌드 단계에서 강제하였습니다. 프론트엔드는 Next.js 16 App Router + React 19로, 백엔드와 동일한 헥사고널 구조를 적용해 API/Mock 어댑터를 단일 파일 수정으로 교체 가능하도록 구성하였습니다.

본 프로젝트의 명시적 목적 중 하나는 **AI 개발 방법론 검증**입니다. Claude Code와의 협업으로 설계→구현→검토 사이클의 리드타임을 단축하고, 헥사고널 아키텍처·ArchUnit·Living Catalog 같은 품질 기제를 소규모 개인 프로젝트에서도 도입 가능한지 실증하는 과정을 병행하고 있습니다.

---

## 모듈 구조

본 프로젝트는 **업무·정산·실적**을 다루는 세 도메인과 별도 관리자 페이지로 구성됩니다. 현재 FMS(업무) 도메인이 우선 구현 완료되었습니다.

| 모듈 | 역할 | 상태 |
|---|---|---|
| **FMS** | 업무 — B/L 발행·EDI·D/O·컨테이너·화물 관리 | 구현 완료 |
| **BMS** | 정산 — 운임 계산·청구서 발행·정산 처리 | 예정 (FMS 안정화 후) |
| **PFM** | 실적 — 집계·분석·대시보드 | 예정 (BMS 이후, P2) |
| **Admin** | 관리자 페이지 — 사용자·권한·코드 테이블 관리 | 예정 (CMS 기획 확정 후) |

### FMS 서브도메인

- **Master B/L** — 해상/항공 수출입 선하증권 발행
- **House B/L** — 해상/항공/육상/Non-BL 혼재 화물 증권
- **Switch B/L** — 원본 B/L 대체 발행

---

## 아키텍처

### 백엔드 — 헥사고널 4계층 + ArchUnit 강제

```
domain                   ← POJO 엔티티·Port·VO·Enum (Spring/JPA 의존 없음)
  ↓
application              ← UseCase 구현 (@Service), 포트 호출
  ↓
adapter/in/web           ← Controller·Assembler·DTO
adapter/out/persistence  ← JpaEntity·QueryDSL·Mapper
```

ArchUnit 1.3으로 의존 방향 위반을 Gradle test 단계에서 자동 감지합니다.

- `domain` → `adapter`·`application`·Spring 클래스 참조 금지
- `@Entity` 어노테이션은 `..adapter.out.persistence..` 패키지 전용
- Source: [`back-end/java-spring/src/test/.../architecture/HexagonalArchitectureTest.java`](back-end/java-spring/src/test/java/com/freightos/fms/architecture/HexagonalArchitectureTest.java)

### 프론트엔드 — 헥사고널 3계층 + bindings.ts 어댑터 스왑

```
domain        ← 순수 타입·엔티티
  ↓
application   ← 포트 인터페이스·유즈케이스 팩토리·bindings.ts
  ↓
adapter/out   ← API 구현 / Mock 구현
```

어댑터 교체 지점: `src/application/{subdomain}/bindings.ts` 한 파일로 API↔Mock 전환. 백엔드 없이 FE 독립 개발이 가능하며, MSW 없이 Mock 어댑터 자체가 테스트 더블 역할을 합니다. 현재 `house-bl` 서브도메인에 적용되어 있습니다([`front-end/src/application/house-bl/bindings.ts`](front-end/src/application/house-bl/bindings.ts)).

### 듀얼 백엔드 순차 전략

Java(Spring Boot) → Python(FastAPI) 순서로 동일 API 스펙을 재구현(병렬 개발 아님). API 계약·DB 스키마·인증 정책을 Java 1단계에서 확정해 Python 구현 시 재설계 비용을 제거합니다.

### 양 스택 공통 표준

| 표준 | 내용 |
|---|---|
| API 계약 | OpenAPI 3.x (springdoc 자동 생성) |
| 에러 응답 | RFC 7807 problem+json |
| 런타임 검증 | 프론트엔드 Zod 4 스키마로 모든 API 응답 파싱 |
| 관측성 | Prometheus 메트릭 (구현) / OpenTelemetry (계획) |
| 로깅 | JSON 구조화 로깅 (`trace_id`·`span_id`·`service` 필드) |

---

## 기술 스택

### Backend

| 기술 | 버전 | 상태 |
|---|---|---|
| Java | 21 LTS | 구현 |
| Spring Boot | 3.4.4 (목표 4.0.x, GA 대기) | 구현 |
| Spring Data JPA + Hibernate | 7 | 구현 |
| QueryDSL | 5.1.0 (jakarta classifier) | 구현 |
| Resilience4j | 2.3.0 | 구현 |
| springdoc-openapi | 2.8.6 | 구현 |
| Spring Boot Actuator + Prometheus | — | 구현 |
| PostgreSQL | 17 + HikariCP | 구현 |
| Flyway | — | 예정 (현재 enabled: false) |
| Spring Security + JWT | — | 예정 |
| Spring Batch 5 | — | 예정 |
| Python 3.13 + FastAPI + SQLAlchemy 2 | — | Phase 2 (미착수) |

### Frontend

| 기술 | 버전 | 상태 |
|---|---|---|
| Next.js App Router | 16.2.4 | 구현 |
| React | 19.2.4 | 구현 |
| TypeScript | 5 strict | 구현 |
| Zustand | 5 (persist 미들웨어) | 구현 |
| TanStack Query / Table / Virtual | 5 / 8 / 3 | 구현 |
| React Hook Form + Zod | 7 / 4 | 구현 |
| Tailwind CSS | v4 (PostCSS) | 구현 |
| @dnd-kit | — | 구현 |
| react-grid-layout | — | 구현 |

### DB · Infra

| 기술 | 상태 |
|---|---|
| PostgreSQL 17 (OLTP/SSOT) | 구현 |
| MongoDB 8 (Analytics Mart) | 예정 (PFM 라운드) |
| Redis 7 | 예정 (Admin 이후) |
| AWS EC2 ASG + RDS + CloudFront + Amplify | 계획 |
| Terraform IaC + GitHub Actions CI/CD | 계획 |

### Test · Quality

| 기술 | 상태 |
|---|---|
| JUnit 5 + Mockito | 구현 |
| ArchUnit 1.3 | 구현 |
| H2 슬라이스 테스트 (@WebMvcTest · @DataJpaTest) | 구현 |
| Vitest 2 + @testing-library/react 16 | 구현 |
| Playwright 1.59 (E2E) | 구현 |
| RFC 7807 problem+json | 구현 |

---

## 디렉토리 구조

```
legacy-to-next/
├── back-end/
│   └── java-spring/
│       └── src/main/java/com/freightos/fms/
│           ├── common/                # 공통 예외·응답·설정
│           ├── domain/                # POJO 엔티티·Port·VO·Enum
│           ├── application/           # UseCase (@Service)
│           └── adapter/
│               ├── in/web/            # Controller·Assembler·DTO
│               └── out/persistence/   # JpaEntity·QueryDSL·Mapper
├── front-end/
│   └── src/
│       ├── domain/                    # 순수 타입·엔티티
│       ├── application/               # 포트 인터페이스·bindings.ts
│       ├── adapter/out/               # API·Mock 어댑터
│       ├── components/                # UI 컴포넌트
│       └── app/
│           ├── (app)/fms/             # FMS 업무 라우트
│           └── (dev)/preview/         # Living Catalog (/preview)
├── schema/
│   ├── V1__fms_initial_schema.sql     # FMS 20개 테이블 초기 DDL
│   └── V_add_house_bl_non_bl_schedule_fields.sql
├── docs/
│   ├── fms/                           # FMS PRD 문서군 (5개 파일)
│   └── portfolio/
│       └── CAREER_REFERENCE.md        # 경력 기술서 1차 자료
├── rules/
│   └── master_coding_rules.md         # 코딩 룰 SSOT
├── .claude/
│   └── agents/PIPELINE.md             # 서브 에이전트 파이프라인 정의
└── CLAUDE.md                          # Claude Code 협업 규칙
```

---

## 실행 방법

### 사전 요구사항

- Java 21+
- Node.js 20+
- PostgreSQL 17 (포트 5432, `back-end/java-spring/src/main/resources/application.yml` 데이터소스 확인)

### 백엔드

```bat
REM 개발 서버 실행
back-end/java-spring/gradlew.bat -p back-end/java-spring bootRun

REM 테스트
back-end/java-spring/gradlew.bat -p back-end/java-spring test

REM 빌드
back-end/java-spring/gradlew.bat -p back-end/java-spring build
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

### 프론트엔드

```bash
# 개발 서버
npm --prefix front-end run dev

# 빌드
npm --prefix front-end run build

# 린트
npm --prefix front-end run lint
```

앱: `http://localhost:3000`  
Living Catalog: `http://localhost:3000/preview`

---

## 진행 상황

### 구현 완료

- **FMS 백엔드**: Master B/L·House B/L·Switch B/L 3 서브도메인, 20개 테이블·20개 도메인 엔티티, 약 35개 테스트 파일·453개 테스트 케이스 (JUnit 5 + Mockito + ArchUnit + H2 슬라이스)
- **FMS 프론트엔드**: 8개 라우트 파일 / `[variant]` 동적 분기 20개 화면 변형 (Sea/Air × Exp/Imp × List/Entry 16 + Truck 2 + Non-BL 2), 27개 도메인 패널, Vitest 65케이스 + Playwright E2E 4 spec
- **표준 컴포넌트 7종**: TextBox·NumberBox·DropBox·CodeBox·LinkBox·RadioBox·TextArea (단위 테스트 보유, Living Catalog에서 관리)
- **아키텍처 기반**: 헥사고널 4계층 (백엔드), 헥사고널 3계층 (프론트엔드), ArchUnit 강제, RFC 7807, Prometheus 메트릭

### 예정

| 영역 | 예정 시점 |
|---|---|
| Spring Security + JWT 인증 | BMS/Admin 착수 전 |
| BMS — 정산 모듈 | FMS 안정화 후 |
| PFM — 실적 모듈 | BMS 이후 (P2) |
| Admin — 관리자 페이지 | CMS 기획 확정 후 |
| Flyway 활성화 | 스키마 확정 후 |
| Python 백엔드 (FastAPI + SQLAlchemy 2) | Java 1단계 완료 후 (Phase 2) |
| AWS 인프라 프로비저닝 | 배포 준비 시 |
| MongoDB Analytics Mart | PFM 라운드 |

---

## 주요 문서

| 문서 | 경로 |
|---|---|
| 경력 기술서 1차 자료 (프로젝트 수치·강점·JD 매핑) | [`docs/portfolio/CAREER_REFERENCE.md`](docs/portfolio/CAREER_REFERENCE.md) |
| FMS PRD — 개요 | [`docs/fms/00_OVERVIEW_20260423_210000.md`](docs/fms/00_OVERVIEW_20260423_210000.md) |
| FMS PRD — 도메인 모델 (E-01~E-24 엔티티) | [`docs/fms/01_DOMAIN_20260424_174104.md`](docs/fms/01_DOMAIN_20260424_174104.md) |
| FMS PRD — 화면 설계 | [`docs/fms/02_SCREENS_20260424_174104.md`](docs/fms/02_SCREENS_20260424_174104.md) |
| FMS PRD — 기술 스택 | [`docs/fms/03_TECH_STACK_20260424_174104.md`](docs/fms/03_TECH_STACK_20260424_174104.md) |
| FMS PRD — 비기능 요구사항 (SLA·성능 KPI·보안) | [`docs/fms/05_NON_FUNCTIONAL_20260424_161404.md`](docs/fms/05_NON_FUNCTIONAL_20260424_161404.md) |
| Claude Code 협업 규칙·빌드 명령어 | [`CLAUDE.md`](CLAUDE.md) |
| 서브 에이전트 파이프라인 정의 | [`.claude/agents/PIPELINE.md`](.claude/agents/PIPELINE.md) |
| 코딩 룰 SSOT | [`rules/master_coding_rules.md`](rules/master_coding_rules.md) |
