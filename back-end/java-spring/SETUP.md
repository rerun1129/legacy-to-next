# Java Backend — 개발 환경 셋업

## 사전 조건
- Java 21 (Temurin 권장: `sdk install java 21-tem`)
- Docker Desktop
- Gradle 8.11+ (`sdk install gradle 8.11.1` 또는 IDE 내장)

## 1. Gradle Wrapper 생성
```bash
cd back-end/java-spring
gradle wrapper --gradle-version=8.11.1
```
> IntelliJ IDEA에서 `build.gradle`을 열면 IDE가 자동으로 wrapper를 제안합니다.

## 2. PostgreSQL 실행
```bash
docker compose up -d
```

## 3. 애플리케이션 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## 4. Swagger UI
http://localhost:8080/swagger-ui.html

## 5. DB 스키마 적용 (Flyway 활성화 전 수동)
스키마 설계 완료 후:
```bash
# 1. schema/ 디렉터리의 V*.sql 파일을 마이그레이션 폴더에 복사
cp ../../schema/V1__fms_initial_schema.sql src/main/resources/db/migration/

# 2. application-local.yml 에서 flyway.enabled=true 로 변경

# 3. 재시작하면 Flyway가 자동 적용
./gradlew bootRun --args='--spring.profiles.active=local'
```

## API 엔드포인트 (v1)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET    | /api/v1/house-bl?jobDiv=SEA&bound=EXP | House B/L 리스트 |
| GET    | /api/v1/house-bl/{id}                 | House B/L 단건 |
| DELETE | /api/v1/house-bl/{id}                 | House B/L 삭제 |
| GET    | /api/v1/master-bl?bound=EXP           | Master B/L 리스트 |
| GET    | /api/v1/master-bl/{id}                | Master B/L 단건 |
| DELETE | /api/v1/master-bl/{id}                | Master B/L 삭제 |

## 프로젝트 구조
```
src/main/java/com/freightos/fms/
├── FmsApplication.java
├── config/
│   ├── JpaAuditingConfig.java     # @CreatedBy / @LastModifiedBy
│   └── OpenApiConfig.java         # Swagger 설정
├── common/
│   ├── entity/BaseEntity.java     # id, createdAt, updatedAt, by
│   ├── exception/                 # FmsException, RFC 7807 핸들러
│   └── response/ApiResponse.java
└── domain/
    ├── housebl/                   # E-08 + 확장 (SEA/AIR/TRUCK/NON_BL)
    │   ├── entity/
    │   ├── enums/
    │   ├── repository/
    │   ├── service/
    │   └── api/
    └── masterbl/                  # E-01 + 확장 (SEA/AIR)
        ├── entity/
        ├── repository/
        └── api/
```
