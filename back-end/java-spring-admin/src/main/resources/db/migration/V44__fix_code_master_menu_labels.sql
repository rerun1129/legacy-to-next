-- Admin: 사이드바 메뉴 라벨 정정
--  - 부모 ADMIN_CODE: 'Common Code'(V43) → 'Code Master' 복구
--  - 자식 ADMIN_CODE_LIST(실제 메뉴 탭): 'List' → 'Common Code'
UPDATE admin.menu
SET label = 'Code Master',
    updated_at = now()
WHERE menu_code = 'ADMIN_CODE';

UPDATE admin.menu
SET label = 'Common Code',
    updated_at = now()
WHERE menu_code = 'ADMIN_CODE_LIST';
