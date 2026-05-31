-- V54: V34(pk_single_id_migration)에서 생성한 백업 테이블 제거.
--   - attribute_value_backup        : V34 attribute_value 단일 PK 전환 직전 스냅샷.
--                                     PK 전환 안정화 완료로 폐기 (V34 "영구 보존" 주석은 본 마이그레이션으로 무효).
--   - admin_user_permission_backup  : V16 에서 원본 테이블이 DROP 된 후 V34 검증용으로 만든 빈 테이블(0건, 실데이터 없음).
-- 둘 다 런타임/JPA·다른 마이그레이션에서 미참조, 의존 FK·INDEX 없음 → 단순 DROP.

DROP TABLE IF EXISTS admin.attribute_value_backup;
DROP TABLE IF EXISTS admin.admin_user_permission_backup;
