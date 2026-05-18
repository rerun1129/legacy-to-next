export interface MeInfo {
  id: number;
  username: string;
  email: string | null;
  attributes: Record<string, string[]>;
  accessibleMenus: string[];
  accessibleButtons: string[];
}

export interface LoginResult {
  accessToken: string;
  refreshToken: string;
  me: MeInfo;
}

export interface RefreshResult {
  accessToken: string;
  refreshToken: string;
}

export interface AuthPort {
  login(username: string, password: string): Promise<LoginResult>;
  refresh(refreshToken: string): Promise<RefreshResult>;
  logout(refreshToken: string): Promise<void>;
  me(): Promise<MeInfo>;
}
