import { authPort } from "@/lib/ports";

export const authUseCases = {
  login: (username: string, password: string) => authPort.login(username, password),
  refresh: (refreshToken: string) => authPort.refresh(refreshToken),
  logout: (refreshToken: string) => authPort.logout(refreshToken),
  me: () => authPort.me(),
};
