import { authPort } from "@/lib/ports";

export const authUseCases = {
  me: (authHeader: string) => authPort.me(authHeader),
};
