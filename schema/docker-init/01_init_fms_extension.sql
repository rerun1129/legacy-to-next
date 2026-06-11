-- 최소 docker-init: fms 스키마 + pgcrypto만 보장하고 테이블 DDL은 FMS Flyway(V1)가 소유.
-- ① admin Flyway V38/V41/V61/V67이 fms.crypt()/fms.gen_salt() 사용 — fresh DB에서 admin-api가
--    fms-api보다 먼저 마이그레이션될 수 있어 여기서 보장한다.
-- ② FMS V1과 중복이지만 둘 다 IF NOT EXISTS라 무해.
-- ③ Flyway 10.x doEmpty()는 익스텐션 소속 객체(pg_depend deptype='e')를 제외하므로
--    이 init이 실행돼도 fms는 빈 스키마로 판정되어 V1이 정상 실행된다(baseline 오인 없음).
--    Flyway 메이저 업그레이드 시 이 전제를 재확인할 것.
CREATE SCHEMA IF NOT EXISTS fms;
CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA fms;
