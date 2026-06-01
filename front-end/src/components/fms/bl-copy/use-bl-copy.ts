"use client";

import { useCallback } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import type { BlQuickSearchItem } from "@/domain/bl-quick-search";
import { houseBlPort, masterBlPort, truckBlPort, nonBlPort } from "@/lib/ports";
import { mapHouseBlDetailToForm } from "@/components/fms/house-bl/map-house-bl-detail";
import { createEmptyHouseBlFormValues } from "@/components/fms/house-bl/house-bl-defaults";
import { mapMasterBlDetailToForm } from "@/components/fms/master-bl/map-master-bl-detail";
import { createEmptyMasterBlFormValues } from "@/components/fms/master-bl/master-bl-defaults";
import { mapTruckBlDetailToForm } from "@/components/fms/truck-bl/map-truck-bl-detail";
import { createEmptyTruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-defaults";
import { mapNonBlDetailToFormValues } from "@/components/fms/non-bl/map-non-bl-detail";
import { createEmptyNonBlFormValues } from "@/components/fms/non-bl/non-bl-defaults";
import { blDraftStore } from "@/lib/use-bl-draft-store";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import type { EntryDomain } from "@/lib/use-entry-focus-store";
import { useTabs } from "@/lib/use-tabs";
import { getPanelFieldsMap } from "./panel-fields-map";

interface CopyTarget {
  href: string;
  variantKey: string;
  blKind: "house" | "master" | "truck" | "non";
  draftKey: string;
  focusDomain: EntryDomain;
  /** React Query 캐시 키 — 엔트리 useQuery와 동일 */
  queryKey: unknown[];
  /** BE 상세 조회 함수 */
  fetchDetail: (id: number) => Promise<unknown>;
  /** BE detail → 폼 값 변환 */
  mapToForm: (detail: unknown) => Record<string, unknown>;
  /** 빈 폼 기본값 팩토리 */
  createEmpty: () => Record<string, unknown>;
}

/** BlQuickSearchItem → copy 대상 정보 해석. Phase B-4: House SEA/AIR + Master SEA/AIR + Truck + Non-BL. */
function resolveCopyTarget(item: BlQuickSearchItem): CopyTarget | null {
  if (item.blType === "HOUSE" && item.jobDiv === "TRUCK") {
    return {
      href: "/fms/truck-bl/entry",
      variantKey: "",
      blKind: "truck",
      // draftKey 콜론 2개: truck::new (truck-bl draft 키 규칙)
      draftKey: "truck::new",
      focusDomain: entryFocusKeys.truckBl,
      queryKey: ["truck-bl", "detail", item.id],
      fetchDetail: (id) => truckBlPort.getById(id),
      mapToForm: (d) => mapTruckBlDetailToForm(d as Parameters<typeof mapTruckBlDetailToForm>[0]) as Record<string, unknown>,
      createEmpty: () => createEmptyTruckBlFormValues() as Record<string, unknown>,
    };
  }

  if (item.blType === "HOUSE" && item.jobDiv === "NON_BL") {
    return {
      href: "/fms/non-bl/entry",
      variantKey: "",
      blKind: "non",
      // draftKey 콜론 2개: non::new (non-bl draft 키 규칙)
      draftKey: "non::new",
      focusDomain: entryFocusKeys.nonBl,
      queryKey: ["non-bl", "detail", item.id],
      fetchDetail: (id) => nonBlPort.getById(id),
      mapToForm: (d) => mapNonBlDetailToFormValues(d as Parameters<typeof mapNonBlDetailToFormValues>[0]) as Record<string, unknown>,
      createEmpty: () => createEmptyNonBlFormValues() as Record<string, unknown>,
    };
  }

  if (item.blType === "HOUSE") {
    const bound = item.bound === "EXP" ? "exp" : "imp";
    const mode = item.jobDiv === "SEA" ? "sea" : item.jobDiv === "AIR" ? "air" : null;
    if (!mode) return null;
    const variantKey = `${mode}-${bound}`;
    return {
      href: `/fms/house-bl/${variantKey}/entry`,
      variantKey,
      blKind: "house",
      draftKey: `house:${variantKey}:new`,
      focusDomain: entryFocusKeys.houseBl(variantKey),
      queryKey: ["house-bl", "detail", item.id],
      fetchDetail: (id) => houseBlPort.getById(id),
      mapToForm: (d) => mapHouseBlDetailToForm(d as Parameters<typeof mapHouseBlDetailToForm>[0]) as Record<string, unknown>,
      createEmpty: () => createEmptyHouseBlFormValues() as Record<string, unknown>,
    };
  }

  if (item.blType === "MASTER") {
    const bound = item.bound === "EXP" ? "exp" : "imp";
    const mode = item.jobDiv === "SEA" ? "sea" : item.jobDiv === "AIR" ? "air" : null;
    if (!mode) return null;
    const variantKey = `${mode}-${bound}`;
    return {
      href: `/fms/master-bl/${variantKey}/entry`,
      variantKey,
      blKind: "master",
      draftKey: `master:${variantKey}:new`,
      focusDomain: entryFocusKeys.masterBl(variantKey),
      queryKey: ["master-bl", "detail", item.id],
      fetchDetail: (id) => masterBlPort.getById(id),
      mapToForm: (d) => mapMasterBlDetailToForm(d as Parameters<typeof mapMasterBlDetailToForm>[0]) as Record<string, unknown>,
      createEmpty: () => createEmptyMasterBlFormValues() as Record<string, unknown>,
    };
  }

  return null;
}

/**
 * 중첩 객체에서 dot-notation 경로로 값을 읽는 유틸.
 */
function getByPath(obj: unknown, path: string): unknown {
  const keys = path.split(".");
  let cur: unknown = obj;
  for (const k of keys) {
    if (cur == null || typeof cur !== "object") return undefined;
    cur = (cur as Record<string, unknown>)[k];
  }
  return cur;
}

/**
 * 중첩 객체에 dot-notation 경로로 값을 쓰는 유틸.
 * 중간 객체가 없으면 생성하지 않음(source에 값이 있을 때만 호출).
 */
function setByPath(obj: Record<string, unknown>, path: string, value: unknown): void {
  const keys = path.split(".");
  let cur = obj;
  for (let i = 0; i < keys.length - 1; i++) {
    const k = keys[i];
    if (cur[k] == null || typeof cur[k] !== "object") {
      cur[k] = {};
    }
    cur = cur[k] as Record<string, unknown>;
  }
  cur[keys[keys.length - 1]] = value;
}

/**
 * selectedPanelKeys에 포함된 패널의 필드 경로만 source → base로 복사해 merged 반환.
 * - 배열 경로("containers", "dims", "scheduleLegs" 등): id 필드를 제거해 신규 레코드로 취급.
 * - 식별자 필드(hbl, mbl, expImp 등 toolbar 키)는 복사 대상이 아니므로
 *   PANEL_FIELDS_MAP에 포함되지 않음 — 설계 상 방어 불필요.
 * - Record<string, unknown> 기반으로 폼 타입에 비종속.
 */
function mergeSelectedPanelFields(
  source: Record<string, unknown>,
  base: Record<string, unknown>,
  blKind: "house" | "master" | "truck" | "non",
  variantKey: string,
  selectedPanelKeys: string[],
): Record<string, unknown> {
  const panelFieldsMap = getPanelFieldsMap(blKind, variantKey);
  const merged = { ...base };

  for (const panelKey of selectedPanelKeys) {
    const fieldPaths = panelFieldsMap[panelKey];
    if (!fieldPaths) continue;

    for (const fieldPath of fieldPaths) {
      const value = getByPath(source, fieldPath);
      if (value === undefined) continue;

      if (Array.isArray(value)) {
        // 배열: 각 행에서 id 제거 — BE가 새 레코드로 INSERT 처리하도록
        const cleanedRows = value.map((row) => {
          if (row == null || typeof row !== "object") return row;
          const rowObj = { ...(row as Record<string, unknown>) };
          delete rowObj["id"];
          return rowObj;
        });
        setByPath(merged, fieldPath, cleanedRows);
      } else {
        setByPath(merged, fieldPath, value);
      }
    }
  }

  return merged;
}

interface UseBlCopyReturn {
  executeCopy: (item: BlQuickSearchItem, selectedPanelKeys: string[]) => Promise<void>;
  isSupported: (item: BlQuickSearchItem) => boolean;
}

/**
 * B/L Copy 실행 훅.
 *
 * executeCopy:
 *   1. blKind별 port.getById(item.id) — 동일 queryKey로 캐시 재사용
 *   2. blKind별 mapDetailToForm으로 source 폼값 생성
 *   3. blKind별 createEmpty() base 생성
 *   4. 선택 panelKey 필드만 source→base 복사 (식별자 미포함)
 *   5. setDraft(draftKey, merged)
 *   6. clearFocus(focusDomain) — edit 탭이 열려있어도 new draft로 전환
 *   7. bumpResetNonce — focus 불변(new→new)일 때도 재초기화 보장
 *   8. addTab + router.push → 새 엔트리 진입
 */
export function useBlCopy(): UseBlCopyReturn {
  const router = useRouter();
  const queryClient = useQueryClient();

  const isSupported = useCallback((item: BlQuickSearchItem): boolean => {
    return resolveCopyTarget(item) !== null;
  }, []);

  const executeCopy = useCallback(async (
    item: BlQuickSearchItem,
    selectedPanelKeys: string[],
  ): Promise<void> => {
    const target = resolveCopyTarget(item);
    if (!target) {
      throw new Error(`B/L Copy: unsupported type blType=${item.blType} jobDiv=${item.jobDiv}`);
    }

    // blKind별 동일 queryKey로 캐시 재사용
    const detail = await queryClient.fetchQuery({
      queryKey: target.queryKey,
      queryFn: () => target.fetchDetail(item.id),
      staleTime: Infinity,
      gcTime: Infinity,
    });

    const sourceValues = target.mapToForm(detail);
    const baseValues = target.createEmpty();
    const merged = mergeSelectedPanelFields(
      sourceValues,
      baseValues,
      target.blKind,
      target.variantKey,
      selectedPanelKeys,
    );

    // draft 주입 후 clearFocus → 엔트리가 new draft를 우선 복원.
    // bumpResetNonce: focus가 이미 undefined(new→new)일 때도 재초기화를 보장.
    blDraftStore.getState().setDraft(target.draftKey, merged);
    useEntryFocusStore.getState().clearFocus(target.focusDomain);
    useEntryFocusStore.getState().bumpResetNonce(target.focusDomain);

    useTabs.getState().addTab(target.href, target.href);
    router.push(target.href);
  }, [queryClient, router]);

  return { executeCopy, isSupported };
}
