package com.freightos.admin.application.userlayout.port.out;

import com.freightos.admin.domain.userlayout.entity.UserUiLayout;

import java.util.Optional;

public interface UserUiLayoutPort {

    Optional<UserUiLayout> findByUserIdAndStorageKey(Long userId, String storageKey);

    UserUiLayout save(UserUiLayout layout);

    void deleteByUserIdAndStorageKey(Long userId, String storageKey);
}
