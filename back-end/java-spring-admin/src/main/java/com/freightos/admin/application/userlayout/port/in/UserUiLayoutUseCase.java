package com.freightos.admin.application.userlayout.port.in;

import java.util.Optional;

public interface UserUiLayoutUseCase {

    /** storageKey에 해당하는 payload JSON 문자열을 반환한다. 없으면 empty. */
    Optional<String> getLayout(String username, String storageKey);

    /** storageKey에 해당하는 레이아웃을 upsert한다. */
    void saveLayout(String username, String storageKey, String payload);

    /** storageKey에 해당하는 레이아웃을 삭제한다. */
    void deleteLayout(String username, String storageKey);
}
