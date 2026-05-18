-- Phase 4 — RBAC 잔존 제거.
-- 기존 admin_user_permission 테이블 DROP (Phase 1 V15에서 attributes로 이행 완료).
-- admin_user.role 컬럼 DROP (Phase 1 V15에서 attributes.role로 이행 완료).

DROP TABLE IF EXISTS admin.admin_user_permission;
ALTER TABLE admin.admin_user DROP COLUMN IF EXISTS role;
