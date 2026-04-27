## 스키마 V2 + Java 전면 리팩토링 완료 (2026-04-27)

master 브랜치에 커밋 완료. `/pipeline-review` 아직 미실행.

**핵심 변경:**
- PK: UUID → `BIGINT GENERATED ALWAYS AS IDENTITY`, 컬럼명 `{테이블명}_id`
- JPA: `@Inheritance(JOINED)` + PFK → extension 테이블이 자체 PK + FK 분리, `@OneToOne(mappedBy, LAZY)`
- 날짜: `LocalDate` / `DATE` → `String` / `VARCHAR(8)` YYYYMMDD
- DB CASCADE 제거 → Adapter에서 extension 먼저 삭제 후 부모 삭제
- 신규 Repository 6개: `MasterBlSeaRepository`, `MasterBlAirRepository`, `HouseBlSea/Air/Truck/NonBlRepository`
- 프론트: `id: number` (Zod `z.number()`), 날짜 `formatDateDisplay()` (`front-end/src/lib/date.ts`)

**컨벤션 문서:** `rules/master_coding_convention.md`
**V2 DDL:** `schema/V2__fms_schema_revision.sql`
