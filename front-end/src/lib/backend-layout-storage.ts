import type { StateStorage } from "zustand/middleware";
import { getSession } from "./admin-session";
import { uiLayoutUseCases } from "@/application/ui-layout/use-cases";

// name별 디바운스 타이머 맵 — 빈번한 레이아웃 변경을 묶어서 1회 PUT으로 처리
const debounceTimers = new Map<string, ReturnType<typeof setTimeout>>();
const DEBOUNCE_MS = 600;

// 위젯·필드 편집 중 백엔드 PUT 보류 플래그 — 저장(commit) 시 resume 후 영속 반영
let layoutPersistPaused = false;
export function setLayoutPersistPaused(v: boolean) { layoutPersistPaused = v; }

export const backendLayoutStorage: StateStorage = {
  async getItem(name: string): Promise<string | null> {
    if (!getSession()) return null;
    try {
      const payload = await uiLayoutUseCases.load(name);
      return payload == null ? null : JSON.stringify(payload);
    } catch {
      // 네트워크 오류 등 예외 시 로컬 기본값으로 폴백
      return null;
    }
  },

  setItem(name: string, value: string): void {
    // 세션 없으면 즉시 중단 — 로그아웃 직후 PUT 방지
    if (!getSession()) return;
    if (layoutPersistPaused) return;   // 편집 중 영속 보류 — 저장(commit) 시점에 resume 후 반영

    const existing = debounceTimers.get(name);
    if (existing) clearTimeout(existing);

    const timer = setTimeout(() => {
      debounceTimers.delete(name);
      // 디바운스 콜백 진입 시 세션 재확인 — 타이머 대기 중 로그아웃이 발생했을 수 있음
      if (!getSession()) return;
      uiLayoutUseCases.save(name, JSON.parse(value)).catch(() => {});
    }, DEBOUNCE_MS);

    debounceTimers.set(name, timer);
  },

  async removeItem(name: string): Promise<void> {
    if (!getSession()) return;
    await uiLayoutUseCases.remove(name).catch(() => {});
  },
};
