import { create } from "zustand";

type EntryDomain = "nonBl" | "truckBl" | `houseBl:${string}` | `masterBl:${string}`;

interface EntryFocusState {
  focus: Partial<Record<EntryDomain, number>>;
  setFocus: (domain: EntryDomain, id: number) => void;
  clearFocus: (domain: EntryDomain) => void;
  getFocus: (domain: EntryDomain) => number | undefined;
}

export const useEntryFocusStore = create<EntryFocusState>()((set, get) => ({
  focus: {},
  setFocus: (domain, id) =>
    set((s) => ({ focus: { ...s.focus, [domain]: id } })),
  clearFocus: (domain) =>
    set((s) => {
      const next = { ...s.focus };
      delete next[domain];
      return { focus: next };
    }),
  getFocus: (domain) => get().focus[domain],
}));

export const entryFocusKeys = {
  nonBl: "nonBl" as const,
  truckBl: "truckBl" as const,
  houseBl: (variantKey: string): EntryDomain => `houseBl:${variantKey}`,
  masterBl: (variantKey: string): EntryDomain => `masterBl:${variantKey}`,
};

/** sidebar navigate href → EntryDomain 매핑. entry path가 아니면 null 반환 */
export function domainFromPath(href: string): EntryDomain | null {
  if (href.startsWith("/fms/non-bl/entry")) return "nonBl";
  if (href.startsWith("/fms/truck-bl/entry")) return "truckBl";
  const m = href.match(/^\/fms\/house-bl\/([^/]+)\/entry/);
  if (m) return `houseBl:${m[1]}`;
  const mm = href.match(/^\/fms\/master-bl\/([^/]+)\/entry/);
  if (mm) return `masterBl:${mm[1]}`;
  return null;
}
