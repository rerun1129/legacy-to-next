-- V60: admin 계정 admin_scope 에 SUBSCRIBER 추가
-- V59 에서 ADMIN_SUBSCRIBER_LIST 메뉴에 admin_scope=SUBSCRIBER 정책을 걸었으나
-- admin 계정이 해당 scope 를 갖지 않아 화면이 노출되지 않는 문제 수정.
-- COALESCE 로 admin_scope 키 부재 시 NULL 병합(jsonb||NULL=NULL) 회피.
-- @> 연산자로 SUBSCRIBER 미보유 행만 업데이트하여 멱등성 보장.

UPDATE admin.admin_user
SET attributes = jsonb_set(
        attributes,
        '{admin_scope}',
        COALESCE(attributes -> 'admin_scope', '[]'::jsonb) || '["SUBSCRIBER"]'::jsonb
    ),
    updated_at = now()
WHERE username = 'admin'
  AND NOT (COALESCE(attributes -> 'admin_scope', '[]'::jsonb) @> '["SUBSCRIBER"]'::jsonb);
