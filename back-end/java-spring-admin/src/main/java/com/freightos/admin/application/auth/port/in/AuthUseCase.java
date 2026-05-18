package com.freightos.admin.application.auth.port.in;

import com.freightos.admin.application.auth.command.LoginCommand;
import com.freightos.admin.application.auth.command.LogoutCommand;
import com.freightos.admin.application.auth.command.RefreshCommand;
import com.freightos.admin.application.auth.projection.LoginResult;
import com.freightos.admin.application.auth.projection.MeProjection;

public interface AuthUseCase {
    LoginResult login(LoginCommand command);
    LoginResult refresh(RefreshCommand command);
    void logout(LogoutCommand command);
    MeProjection getMe(String username);
}
