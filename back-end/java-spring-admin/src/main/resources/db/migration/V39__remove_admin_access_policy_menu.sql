-- V39: Access - Policy 관리 메뉴/버튼/정책 row 제거
--
-- Permission Preset 도입(V33~V37) 후 menu_policy/button_policy CRUD UI 는 운영에 필요 없음.
-- BE 의 ABAC 평가(PolicyEvaluator + admin.menu_policy / admin.button_policy 테이블+데이터)는
-- 그대로 유지. ADMIN_ACCESS_POLICY 메뉴와 그 자식 버튼(3건) + 거기에 연결된 정책 row 만 정리.
--
-- 다른 메뉴/버튼의 정책 row 는 영향 없음.

-- 1) 자식 button 들의 button_policy 먼저 삭제 (FK 의존: button_policy → button)
DELETE FROM admin.button_policy
WHERE button_id IN (
    SELECT b.button_id
    FROM admin.button b
    JOIN admin.menu m ON m.menu_id = b.menu_id
    WHERE m.menu_code = 'ADMIN_ACCESS_POLICY'
);

-- 2) 메뉴의 menu_policy 삭제 (FK 의존: menu_policy → menu)
DELETE FROM admin.menu_policy
WHERE menu_id = (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_POLICY');

-- 3) 자식 button 삭제 (FK 의존: button → menu)
DELETE FROM admin.button
WHERE menu_id = (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_POLICY');

-- 4) 메뉴 row 삭제
DELETE FROM admin.menu
WHERE menu_code = 'ADMIN_ACCESS_POLICY';
