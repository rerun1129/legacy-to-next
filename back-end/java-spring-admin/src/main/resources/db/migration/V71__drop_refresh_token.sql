-- 게이트웨이 Redis 세션 전환(Phase 3)으로 refresh token DB 영속 미사용 — 테이블 제거.
-- FK fk_refresh_token_user / UQ uq_refresh_token_hash / IX ix_refresh_token_user_id 는
-- refresh_token 테이블 소속이므로 테이블 DROP 시 동반 드랍됨.
DROP TABLE admin.refresh_token;
