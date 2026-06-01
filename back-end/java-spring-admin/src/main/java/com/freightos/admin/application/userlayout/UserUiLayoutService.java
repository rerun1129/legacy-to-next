package com.freightos.admin.application.userlayout;

import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.application.userlayout.port.in.UserUiLayoutUseCase;
import com.freightos.admin.application.userlayout.port.out.UserUiLayoutPort;
import com.freightos.admin.domain.user.entity.AdminUser;
import com.freightos.admin.domain.userlayout.entity.UserUiLayout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserUiLayoutService implements UserUiLayoutUseCase {

    private final UserUiLayoutPort userUiLayoutPort;
    private final UserUseCase userUseCase;

    @Override
    public Optional<String> getLayout(String username, String storageKey) {
        AdminUser user = userUseCase.findUserByUsername(username);
        return userUiLayoutPort.findByUserIdAndStorageKey(user.getId(), storageKey)
                .map(UserUiLayout::getPayload);
    }

    @Override
    @Transactional
    public void saveLayout(String username, String storageKey, String payload) {
        AdminUser user = userUseCase.findUserByUsername(username);
        Optional<UserUiLayout> existing = userUiLayoutPort.findByUserIdAndStorageKey(user.getId(), storageKey);
        if (existing.isPresent()) {
            UserUiLayout layout = existing.get();
            layout.updatePayload(payload);
            userUiLayoutPort.save(layout);
        } else {
            userUiLayoutPort.save(UserUiLayout.create(user.getId(), storageKey, payload));
        }
    }

    @Override
    @Transactional
    public void deleteLayout(String username, String storageKey) {
        AdminUser user = userUseCase.findUserByUsername(username);
        userUiLayoutPort.deleteByUserIdAndStorageKey(user.getId(), storageKey);
    }
}
