package com.freightos.admin.application.user;

import com.freightos.admin.application.user.command.CreateUserCommand;
import com.freightos.admin.application.user.command.UpdateUserCommand;
import com.freightos.admin.application.user.port.out.UserPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.user.entity.AdminUser;
import com.freightos.admin.domain.user.entity.Permission;
import com.freightos.admin.domain.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserPort userPort;

    @Mock
    private UserFactory userFactory;

    @InjectMocks
    private UserService userService;

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_callsFactoryAndPortSaveReturnsId() {
        CreateUserCommand command = new CreateUserCommand("alice", "alice@example.com", "pass1234", UserRole.USER, true, Set.of());
        AdminUser domain = AdminUser.create("alice", "alice@example.com", "hashed", UserRole.USER, true, Set.of());
        given(userFactory.from(command)).willReturn(domain);
        given(userPort.save(domain)).willReturn(7L);

        Long id = userService.createUser(command);

        assertThat(id).isEqualTo(7L);
        then(userFactory).should().from(command);
        then(userPort).should().save(domain);
        then(userPort).should().savePermissions(7L, Set.of());
    }

    // ── findUserById: not found → ApplicationException 404 ───────────────────

    @Test
    void findUserById_notFound_throwsNotFound() {
        given(userPort.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("USER_NOT_FOUND");
                });
    }

    // ── findUserById: present → 도메인 반환 ───────────────────────────────────

    @Test
    void findUserById_found_returnsDomain() {
        AdminUser domain = AdminUser.create("alice", "alice@example.com", "hashed", UserRole.USER, true, Set.of());
        given(userPort.findById(1L)).willReturn(Optional.of(domain));

        AdminUser result = userService.findUserById(1L);

        assertThat(result).isEqualTo(domain);
    }

    // ── findUserByUsername: not found → ApplicationException 404 ─────────────

    @Test
    void findUserByUsername_notFound_throwsNotFound() {
        given(userPort.findByUsername("ghost")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByUsername("ghost"))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("USER_NOT_FOUND");
                });
    }

    // ── updateUser: 일반 update → applyUpdate + port.update ──────────────────

    @Test
    void updateUser_normal_callsPortUpdate() {
        AdminUser existing = AdminUser.create("alice", "alice@example.com", "hashed", UserRole.USER, true, Set.of());
        UpdateUserCommand command = new UpdateUserCommand("new@example.com", null, UserRole.USER, true, Set.of());
        given(userPort.findById(1L)).willReturn(Optional.of(existing));
        given(userFactory.hashIfPresent(null)).willReturn(null);

        userService.updateUser(1L, command);

        then(userPort).should().update(eq(1L), any(AdminUser.class));
        then(userPort).should().savePermissions(1L, Set.of());
    }

    // ── updateUser: 마지막 ADMIN 비활성 시도 → conflict ──────────────────────

    @Test
    void updateUser_lastAdminDeactivate_throwsConflict() {
        AdminUser existing = AdminUser.create("admin", null, "hashed", UserRole.ADMIN, true, Set.of());
        UpdateUserCommand command = new UpdateUserCommand(null, null, UserRole.ADMIN, false, Set.of());
        given(userPort.findById(1L)).willReturn(Optional.of(existing));
        given(userPort.countActiveByRole(UserRole.ADMIN)).willReturn(1L);

        assertThatThrownBy(() -> userService.updateUser(1L, command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("LAST_ADMIN");
                });
    }

    // ── updateUser: passwordHash null → applyUpdate에 null 전달 ──────────────

    @Test
    void updateUser_nullPassword_hashIfPresentReturnsNull() {
        AdminUser existing = AdminUser.create("alice", "alice@example.com", "original_hash", UserRole.USER, true, Set.of());
        UpdateUserCommand command = new UpdateUserCommand("new@example.com", null, UserRole.USER, true, Set.of());
        given(userPort.findById(1L)).willReturn(Optional.of(existing));
        given(userFactory.hashIfPresent(null)).willReturn(null);

        userService.updateUser(1L, command);

        // passwordHash null 전달 → applyUpdate에서 기존 hash 유지 확인
        assertThat(existing.getPasswordHash()).isEqualTo("original_hash");
        then(userFactory).should().hashIfPresent(null);
    }

    // ── deleteUser: 일반 → port.softDelete ───────────────────────────────────

    @Test
    void deleteUser_normal_callsSoftDelete() {
        AdminUser existing = AdminUser.create("alice", "alice@example.com", "hashed", UserRole.USER, true, Set.of());
        given(userPort.findById(5L)).willReturn(Optional.of(existing));

        userService.deleteUser(5L);

        then(userPort).should().softDelete(5L);
    }

    // ── deleteUser: 마지막 ADMIN 삭제 시도 → conflict ────────────────────────

    @Test
    void deleteUser_lastAdmin_throwsConflict() {
        AdminUser existing = AdminUser.create("admin", null, "hashed", UserRole.ADMIN, true, Set.of());
        given(userPort.findById(1L)).willReturn(Optional.of(existing));
        given(userPort.countActiveByRole(UserRole.ADMIN)).willReturn(1L);

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("LAST_ADMIN");
                });
    }
}
