# java-spring-gateway (API Gateway)

Spring Cloud Gateway(WebFlux) 단일 진입점. 포트 **8084**. FE의 모든 API 호출이 이곳을 경유한다.

## 라우팅

| 경로 | 대상 | 비고 |
|---|---|---|
| `/api/admin/**` | admin-api:8081 | |
| `/api/teams/**` | admin-api:8081 | admin TeamController가 prefix 미준수(레거시) — 정규화 검토 항목 |
| `/api/bms/**` | bms-api:8082 | |
| `/api/pms/**` | pms-api:8083 | |
| `/api/**` (catch-all, order 10) | fms-api:8080 | FMS는 모듈 prefix 없음 |

라우트별 CircuitBreaker + `forward:/fallback/{module}` 503 JSON. TimeLimiter 60s(pmsCb는 mart rebuild 대비 600s).
CORS는 게이트웨이 globalcors 전담(모듈에는 CORS 설정 없음).

## 인증 — JWT 중앙 검증 + 신뢰 헤더 (Phase 2)

게이트웨이가 `Authorization: Bearer`를 검증(HS256, admin 발급과 동일 `JWT_SECRET`)하고 다운스트림에 헤더 주입:

| 헤더 | 값 |
|---|---|
| `X-Auth-User` | JWT `sub` (username) |
| `X-Auth-Authorities` | `auth` 클레임 (comma-separated) |
| `X-Auth-Attr` | `attr` 클레임 JSON의 UTF-8→Base64 (attr은 JWT에선 Map — 게이트웨이가 직렬화) |
| `X-Internal-Token` | 내부 공유키 — **항상** 주입 |

- 클라이언트가 보낸 위 4개 헤더는 게이트웨이가 **무조건 제거**(위조 차단)
- 토큰 없음/무효 → X-Auth-* 없이 통과(enrich-only). 보호 여부는 각 모듈 SecurityConfig가 결정
- 모듈은 `HeaderAuthenticationFilter`로 내부키 일치 + X-Auth-User 존재 시에만 SecurityContext 구성
- docker-compose에서 모듈 4종은 host 포트 비공개 — 게이트웨이(8084)만 노출

## 로컬 bootRun 직접 디버깅 (게이트웨이 우회 시)

모듈을 bootRun으로 직접 띄워 호출할 때는 신뢰 헤더를 수동 부착해야 한다:

```
curl http://localhost:8080/api/enums/Bound \
  -H "X-Internal-Token: dev-internal-gateway-key-change-me" \
  -H "X-Auth-User: admin" \
  -H "X-Auth-Authorities: <필요 권한 CSV>"
```

(기본 내부키는 각 모듈 application.yml의 `gateway.internal-token` 기본값. 게이트웨이까지 같이 bootRun하면 헤더 없이 `http://localhost:8084` 경유로 평소처럼 호출 가능 — 라우트 URI 기본값이 localhost를 가리킴.)

## 환경변수

| env | 용도 | 기본값 |
|---|---|---|
| `JWT_SECRET` | 토큰 검증(HS256, admin 발급과 공유) | dev 기본값 |
| `INTERNAL_GATEWAY_KEY` | 다운스트림 신뢰 헤더 내부키 | `dev-internal-gateway-key-change-me` |
| `FMS_API_URI` 등 4종 | 라우트 대상 (docker=서비스명, 로컬=localhost) | `http://localhost:808x` |
| `APP_CORS_ALLOWED_ORIGINS` | 허용 오리진 | `http://localhost:3000` |
