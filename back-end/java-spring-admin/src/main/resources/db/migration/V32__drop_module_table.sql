-- 1) ADMIN_ACCESS_MODULE 메뉴 및 관련 button/policy 정리
DELETE FROM admin.button_policy WHERE button_id IN (
  SELECT button_id FROM admin.button
   WHERE menu_id = (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_MODULE')
);
DELETE FROM admin.button WHERE menu_id = (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_MODULE');
DELETE FROM admin.menu_policy WHERE menu_id = (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_MODULE');
DELETE FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_MODULE';

-- 2) admin.menu.module_code FK 제거
ALTER TABLE admin.menu DROP CONSTRAINT fk_admin_menu_module;

-- 3) admin.module 테이블 제거
DROP TABLE admin.module;
