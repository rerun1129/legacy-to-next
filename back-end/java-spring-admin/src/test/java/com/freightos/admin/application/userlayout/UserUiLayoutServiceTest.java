package com.freightos.admin.application.userlayout;

import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.application.userlayout.port.out.UserUiLayoutPort;
import com.freightos.admin.domain.user.entity.AdminUser;
import com.freightos.admin.domain.userlayout.entity.UserUiLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class UserUiLayoutServiceTest {

    @Mock
    private UserUiLayoutPort userUiLayoutPort;

    @Mock
    private UserUseCase userUseCase;

    @InjectMocks
    private UserUiLayoutService userUiLayoutService;

    private static final String USERNAME = "testuser";
    private static final String STORAGE_KEY = "fms.widgetLayouts.v1";
    private static final String PAYLOAD = "{\"cols\":12}";
    private static final Long USER_ID = 1L;

    private AdminUser stubUser() {
        AdminUser user = AdminUser.create(USERNAME, "test@test.com", "hash", true, Collections.emptyMap(), null, null);
        user.assignIdentity(USER_ID, null, null, null, null);
        return user;
    }

    // ── getLayout: 레이아웃 존재 → payload 반환 ─────────────────────────────────

    @Test
    void getLayout_exists_returnsPayload() {
        AdminUser user = stubUser();
        UserUiLayout layout = UserUiLayout.create(USER_ID, STORAGE_KEY, PAYLOAD);
        given(userUseCase.findUserByUsername(USERNAME)).willReturn(user);
        given(userUiLayoutPort.findByUserIdAndStorageKey(USER_ID, STORAGE_KEY)).willReturn(Optional.of(layout));

        Optional<String> result = userUiLayoutService.getLayout(USERNAME, STORAGE_KEY);

        assertThat(result).isPresent().contains(PAYLOAD);
    }

    // ── getLayout: 레이아웃 없음 → empty ─────────────────────────────────────

    @Test
    void getLayout_notExists_returnsEmpty() {
        AdminUser user = stubUser();
        given(userUseCase.findUserByUsername(USERNAME)).willReturn(user);
        given(userUiLayoutPort.findByUserIdAndStorageKey(USER_ID, STORAGE_KEY)).willReturn(Optional.empty());

        Optional<String> result = userUiLayoutService.getLayout(USERNAME, STORAGE_KEY);

        assertThat(result).isEmpty();
    }

    // ── saveLayout: 기존 없음 → create 후 save (insert 분기) ─────────────────

    @Test
    void saveLayout_noExisting_callsCreate() {
        AdminUser user = stubUser();
        given(userUseCase.findUserByUsername(USERNAME)).willReturn(user);
        given(userUiLayoutPort.findByUserIdAndStorageKey(USER_ID, STORAGE_KEY)).willReturn(Optional.empty());
        given(userUiLayoutPort.save(any(UserUiLayout.class))).willAnswer(inv -> inv.getArgument(0));

        userUiLayoutService.saveLayout(USERNAME, STORAGE_KEY, PAYLOAD);

        then(userUiLayoutPort).should().save(any(UserUiLayout.class));
    }

    // ── saveLayout: 기존 존재 → updatePayload 후 save (update 분기) ──────────

    @Test
    void saveLayout_existing_callsUpdatePayload() {
        AdminUser user = stubUser();
        UserUiLayout existing = UserUiLayout.create(USER_ID, STORAGE_KEY, "{\"cols\":6}");
        existing.assignIdentity(10L, null, null);

        given(userUseCase.findUserByUsername(USERNAME)).willReturn(user);
        given(userUiLayoutPort.findByUserIdAndStorageKey(USER_ID, STORAGE_KEY)).willReturn(Optional.of(existing));
        given(userUiLayoutPort.save(any(UserUiLayout.class))).willAnswer(inv -> inv.getArgument(0));

        userUiLayoutService.saveLayout(USERNAME, STORAGE_KEY, PAYLOAD);

        // updatePayload가 호출된 결과로 payload가 새 값으로 바뀌어 save에 전달되어야 한다
        assertThat(existing.getPayload()).isEqualTo(PAYLOAD);
        then(userUiLayoutPort).should().save(existing);
    }

    // ── deleteLayout: port.deleteByUserIdAndStorageKey 호출 ─────────────────

    @Test
    void deleteLayout_callsPort() {
        AdminUser user = stubUser();
        given(userUseCase.findUserByUsername(USERNAME)).willReturn(user);
        willDoNothing().given(userUiLayoutPort).deleteByUserIdAndStorageKey(USER_ID, STORAGE_KEY);

        userUiLayoutService.deleteLayout(USERNAME, STORAGE_KEY);

        then(userUiLayoutPort).should().deleteByUserIdAndStorageKey(USER_ID, STORAGE_KEY);
    }
}
