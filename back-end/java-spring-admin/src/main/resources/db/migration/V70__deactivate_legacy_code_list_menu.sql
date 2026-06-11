-- V70: 레거시 Code List(/admin/code/list) 메뉴·버튼 비활성화.
-- code_master/code_detail 버티컬은 신규 공통코드 화면(V69, ADMIN_COMMON_CODE)으로 대체됨.
-- deactivate-only 원칙 — 하드 DELETE 금지. menu_policy·button_policy 행은 존치.
-- 테이블 DROP 없음(code_master/code_detail 테이블 유지).

-- ==================== 1. 메뉴 비활성화 ====================
UPDATE admin.menu
SET active     = false,
    updated_at = now()
WHERE menu_code = 'ADMIN_CODE_LIST';

-- ==================== 2. 버튼 비활성화 (4종: RESET/SEARCH/SAVE/DETAIL_SAVE) ====================
UPDATE admin.button
SET active     = false,
    updated_at = now()
WHERE menu_id = (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_LIST');
