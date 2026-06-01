import { uiLayoutPort } from "@/lib/ports";

export const uiLayoutUseCases = {
  load(storageKey: string): Promise<unknown | null> {
    return uiLayoutPort.load(storageKey);
  },

  save(storageKey: string, payload: unknown): Promise<void> {
    return uiLayoutPort.save(storageKey, payload);
  },

  remove(storageKey: string): Promise<void> {
    return uiLayoutPort.remove(storageKey);
  },
};
