# FMS DDL 코딩 컨벤션

## 1. PK 컬럼명 규칙

PK 컬럼명은 `{테이블명}_id` 형식으로 명명한다.

- 예: `master_bl` 테이블의 PK → `master_bl_id`
- 예: `house_bl_container` 테이블의 PK → `house_bl_container_id`
- 단순 `id` 컬럼명 사용 금지.

## 2. PK 타입 규칙

PK 타입은 UUID를 사용하지 않고 무조건 `BIGINT GENERATED ALWAYS AS IDENTITY`를 사용한다.

- `gen_random_uuid()` 또는 UUID 타입 사용 금지.
- 예:

```sql
master_bl_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
```

## 3. 자식/확장 테이블 PK·FK 분리 규칙

PFK(Primary-Foreign-Key) 전략 사용 금지. 부모의 PK를 자식 테이블의 PK로 재사용하지 않는다.

자식(확장) 테이블은 자체 BIGINT IDENTITY PK와 부모를 참조하는 별도 FK 컬럼을 각각 독립적으로 갖는다.

JPA 매핑: `@Inheritance(JOINED)` + `@PrimaryKeyJoinColumn` 대신 `@OneToOne` 독립 엔티티로 구현.

예:

```sql
CREATE TABLE master_bl_sea (
    master_bl_sea_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_bl_id      BIGINT NOT NULL REFERENCES master_bl(master_bl_id),
    ...
);
```

## 4. 날짜 컬럼 타입 규칙

비즈니스 날짜 컬럼은 `DATE` 타입을 사용하지 않고 `VARCHAR(8)`을 사용한다. (YYYYMMDD 8자리 형식)

- 대상: ETD, ETA, 선적일, 발행일, 도착예정일 등 모든 비즈니스 날짜.
- 예외: `created_at`, `updated_at` 등 감사(audit) 타임스탬프는 `TIMESTAMPTZ` 유지.
- 날짜 유효성 검증은 애플리케이션 레벨에서 수행한다.

## 5. ON DELETE CASCADE 금지 규칙

DB 레벨 `ON DELETE CASCADE` 사용 금지.

부모 레코드 삭제 시 자식 레코드는 애플리케이션 레벨에서 명시적으로 삭제 처리한다.

JPA CascadeType은 ORM 레벨이므로 허용.

## 6. 컬럼 설명 규칙

DDL 내 인라인 `--` 컬럼 설명 주석을 사용하지 않는다.

컬럼 설명은 `COMMENT ON COLUMN` 구문을 사용한다.

약어, 코드성 컬럼(job_div, bound, pol_code, bl_type 등)에는 반드시 설명을 작성한다.

예:

```sql
COMMENT ON COLUMN master_bl.job_div IS '운송구분: SEA(해상) | AIR(항공)';
COMMENT ON COLUMN master_bl.bound   IS '수출입구분: EXP(수출) | IMP(수입)';
```
