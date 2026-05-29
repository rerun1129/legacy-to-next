-- FE(ActionButton)·BE(@PreAuthorize) 어디서도 참조되지 않는 사문화 버튼 4개를 카탈로그에서 제거.

DELETE FROM admin.button_policy
 WHERE button_id IN (SELECT button_id FROM admin.button
                      WHERE button_code IN ('ADMIN_ACCESS_MENU_CREATE',
                                            'ADMIN_ACCESS_BUTTON_CREATE',
                                            'ADMIN_ACCESS_ATTRIBUTE_CREATE',
                                            'ADMIN_ACCESS_ATTRIBUTE_UPDATE'));

DELETE FROM admin.button
 WHERE button_code IN ('ADMIN_ACCESS_MENU_CREATE',
                       'ADMIN_ACCESS_BUTTON_CREATE',
                       'ADMIN_ACCESS_ATTRIBUTE_CREATE',
                       'ADMIN_ACCESS_ATTRIBUTE_UPDATE');
