export interface UiLayoutPort {
  load(storageKey: string): Promise<unknown | null>;
  save(storageKey: string, payload: unknown): Promise<void>;
  remove(storageKey: string): Promise<void>;
}
