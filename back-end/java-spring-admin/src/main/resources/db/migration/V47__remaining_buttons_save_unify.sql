-- V47: 나머지 5개 화면(Code List/User/Customer/Permission Preset/Access Attribute)의 CREATE/UPDATE/DELETE 버튼을 SAVE 단일 버튼으로 통일.

-- ========== 1. SAVE 버튼 7건 INSERT ==========

INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order)
VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_LIST'),                'ADMIN_CODE_LIST_SAVE',                'Save',       'CUSTOM', 12),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_LIST'),                'ADMIN_CODE_LIST_DETAIL_SAVE',         'Detail Save', 'CUSTOM', 13),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_USER_LIST'),                'ADMIN_USER_LIST_SAVE',                'Save',       'CUSTOM', 12),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CUSTOMER_LIST'),            'ADMIN_CUSTOMER_LIST_SAVE',            'Save',       'CUSTOM', 12),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_PERMISSION_PRESET'), 'ADMIN_ACCESS_PERMISSION_PRESET_SAVE', 'Save',       'CUSTOM', 12),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_ATTRIBUTE'),         'ADMIN_ACCESS_ATTRIBUTE_SAVE',         'Save',       'CUSTOM', 12),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_ATTRIBUTE'),         'ADMIN_ACCESS_ATTRIBUTE_VALUE_SAVE',   'Value Save', 'CUSTOM', 13)
ON CONFLICT (button_code) DO NOTHING;

-- ========== 2. SAVE 버튼 policy 7건 INSERT ==========

INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT button_id, 'module', 'ADMIN'
FROM admin.button
WHERE button_code IN (
    'ADMIN_CODE_LIST_SAVE',
    'ADMIN_CODE_LIST_DETAIL_SAVE',
    'ADMIN_USER_LIST_SAVE',
    'ADMIN_CUSTOMER_LIST_SAVE',
    'ADMIN_ACCESS_PERMISSION_PRESET_SAVE',
    'ADMIN_ACCESS_ATTRIBUTE_SAVE',
    'ADMIN_ACCESS_ATTRIBUTE_VALUE_SAVE'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;

-- ========== 3. 구 CREATE/UPDATE/DELETE button_policy 삭제 (FK 선행) ==========

DELETE FROM admin.button_policy
WHERE button_id IN (
    SELECT button_id FROM admin.button
    WHERE button_code IN (
        'ADMIN_CODE_LIST_CREATE',                    'ADMIN_CODE_LIST_UPDATE',                    'ADMIN_CODE_LIST_DELETE',
        'ADMIN_USER_LIST_CREATE',                    'ADMIN_USER_LIST_UPDATE',                    'ADMIN_USER_LIST_DELETE',
        'ADMIN_CUSTOMER_LIST_CREATE',                'ADMIN_CUSTOMER_LIST_UPDATE',                'ADMIN_CUSTOMER_LIST_DELETE',
        'ADMIN_ACCESS_PERMISSION_PRESET_CREATE',     'ADMIN_ACCESS_PERMISSION_PRESET_UPDATE',     'ADMIN_ACCESS_PERMISSION_PRESET_DELETE',
        'ADMIN_ACCESS_ATTRIBUTE_CREATE',             'ADMIN_ACCESS_ATTRIBUTE_UPDATE',             'ADMIN_ACCESS_ATTRIBUTE_DELETE'
    )
);

-- ========== 4. 구 CREATE/UPDATE/DELETE 버튼 15건 삭제 ==========

DELETE FROM admin.button
WHERE button_code IN (
    'ADMIN_CODE_LIST_CREATE',                    'ADMIN_CODE_LIST_UPDATE',                    'ADMIN_CODE_LIST_DELETE',
    'ADMIN_USER_LIST_CREATE',                    'ADMIN_USER_LIST_UPDATE',                    'ADMIN_USER_LIST_DELETE',
    'ADMIN_CUSTOMER_LIST_CREATE',                'ADMIN_CUSTOMER_LIST_UPDATE',                'ADMIN_CUSTOMER_LIST_DELETE',
    'ADMIN_ACCESS_PERMISSION_PRESET_CREATE',     'ADMIN_ACCESS_PERMISSION_PRESET_UPDATE',     'ADMIN_ACCESS_PERMISSION_PRESET_DELETE',
    'ADMIN_ACCESS_ATTRIBUTE_CREATE',             'ADMIN_ACCESS_ATTRIBUTE_UPDATE',             'ADMIN_ACCESS_ATTRIBUTE_DELETE'
);
