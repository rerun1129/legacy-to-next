package com.freightos.admin.application.user;

import com.freightos.admin.application.user.command.CreateUserCommand;
import com.freightos.admin.domain.user.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFactory {

    private final PasswordEncoder passwordEncoder;

    public AdminUser from(CreateUserCommand command) {
        return AdminUser.create(
                command.username(),
                command.email(),
                passwordEncoder.encode(command.rawPassword()),
                command.role(),
                command.active(),
                command.permissions()
        );
    }

    public String hashIfPresent(String rawPasswordOrNull) {
        if (rawPasswordOrNull == null || rawPasswordOrNull.isBlank()) return null;
        return passwordEncoder.encode(rawPasswordOrNull);
    }
}
