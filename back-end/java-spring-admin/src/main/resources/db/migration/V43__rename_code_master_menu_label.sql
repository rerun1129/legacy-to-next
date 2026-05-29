-- Admin: 사이드바 메뉴 'Code Master' 라벨을 'Common Code' 로 변경 (menu_code = ADMIN_CODE)
UPDATE admin.menu
SET label = 'Common Code',
    updated_at = now()
WHERE menu_code = 'ADMIN_CODE';
