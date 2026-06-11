package com.freightos.admin.application.auth.port.in;

import com.freightos.admin.application.auth.command.LoginCommand;
import com.freightos.admin.application.auth.projection.LoginResult;
import com.freightos.admin.application.auth.projection.MeProjection;

public interface AuthUseCase {
    LoginResult login(LoginCommand command);
    MeProjection getMe(String username);
}
