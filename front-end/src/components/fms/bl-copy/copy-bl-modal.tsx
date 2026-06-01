"use client";

import { useState, useCallback, useMemo } from "react";
import { useTranslations } from "next-intl";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import type { BlQuickSearchItem } from "@/domain/bl-quick-search";
import { HOUSE_BL_SEA_REGISTRY } from "@/components/fms/house-bl/tabs/main-sea";
import { HOUSE_BL_AIR_EXP_REGISTRY, HOUSE_BL_AIR_IMP_REGISTRY } from "@/components/fms/house-bl/tabs/main-air";
import { MASTER_BL_SEA_PANEL_LIST } from "@/components/fms/master-bl/tabs/main-sea";
import { MASTER_BL_AIR_EXP_REGISTRY, MASTER_BL_AIR_IMP_REGISTRY } from "@/components/fms/master-bl/tabs/main-air";
import { TRUCK_REGISTRY } from "@/components/fms/truck-bl/tabs/main-truck";
import { NON_BL_REGISTRY } from "@/components/fms/non-bl/tabs/main-non-bl";
import { getPanelFieldsMap } from "./panel-fields-map";
import { useBlCopy } from "./use-bl-copy";

/** house-bl-grid는 자식 BL 참조 그리드 — Copy 의미 없음 */
const EXCLUDED_PANEL_KEYS = new Set(["house-bl-grid"]);

interface PanelListEntry {
  key: string;
  label: string;
}

interface Props {
  item: BlQuickSearchItem;
  isOpen: boolean;
  onClose: () => void;
}

/**
 * item의 blType/jobDiv/bound → 체크박스 패널 목록 해석.
 * house-bl-grid는 항상 제외.
 * Master SEA는 MASTER_BL_SEA_PANEL_LIST(form 없이 key/label만), AIR는 BASE 상수 배열 사용.
 * Truck은 TRUCK_REGISTRY(form 비의존 정적 배열) 직접 사용.
 * Non-BL은 NON_BL_REGISTRY(form 비의존 정적 배열) 직접 사용.
 */
function resolvePanelList(item: BlQuickSearchItem): PanelListEntry[] | null {
  if (item.blType === "HOUSE" && item.jobDiv === "TRUCK") {
    return TRUCK_REGISTRY
      .filter((w) => !EXCLUDED_PANEL_KEYS.has(w.key))
      .map((w) => ({ key: w.key, label: w.label }));
  }
  if (item.blType === "HOUSE" && item.jobDiv === "NON_BL") {
    return NON_BL_REGISTRY
      .filter((w) => !EXCLUDED_PANEL_KEYS.has(w.key))
      .map((w) => ({ key: w.key, label: w.label }));
  }
  if (item.blType === "HOUSE" && item.jobDiv === "SEA") {
    return HOUSE_BL_SEA_REGISTRY
      .filter((w) => !EXCLUDED_PANEL_KEYS.has(w.key))
      .map((w) => ({ key: w.key, label: w.label }));
  }
  if (item.blType === "HOUSE" && item.jobDiv === "AIR") {
    const base = item.bound === "EXP" ? HOUSE_BL_AIR_EXP_REGISTRY : HOUSE_BL_AIR_IMP_REGISTRY;
    return base
      .filter((w) => !EXCLUDED_PANEL_KEYS.has(w.key))
      .map((w) => ({ key: w.key, label: w.label }));
  }
  if (item.blType === "MASTER" && item.jobDiv === "SEA") {
    return MASTER_BL_SEA_PANEL_LIST
      .filter((w) => !EXCLUDED_PANEL_KEYS.has(w.key))
      .map((w) => ({ key: w.key, label: w.label }));
  }
  if (item.blType === "MASTER" && item.jobDiv === "AIR") {
    const base = item.bound === "EXP" ? MASTER_BL_AIR_EXP_REGISTRY : MASTER_BL_AIR_IMP_REGISTRY;
    return base
      .filter((w) => !EXCLUDED_PANEL_KEYS.has(w.key))
      .map((w) => ({ key: w.key, label: w.label }));
  }
  return null;
}

/** item → blKind / variantKey 해석 */
function resolveBlMeta(item: BlQuickSearchItem): {
  blKind: "house" | "master" | "truck" | "non";
  variantKey: string;
} | null {
  if (item.blType === "HOUSE" && item.jobDiv === "TRUCK") {
    // Truck은 variant 없음 — variantKey 빈 문자열
    return { blKind: "truck", variantKey: "" };
  }
  if (item.blType === "HOUSE" && item.jobDiv === "NON_BL") {
    // Non-BL은 variant 없음 — variantKey 빈 문자열
    return { blKind: "non", variantKey: "" };
  }
  if (item.blType === "HOUSE" && (item.jobDiv === "SEA" || item.jobDiv === "AIR")) {
    const mode = item.jobDiv === "SEA" ? "sea" : "air";
    const bound = item.bound === "EXP" ? "exp" : "imp";
    return { blKind: "house", variantKey: `${mode}-${bound}` };
  }
  if (item.blType === "MASTER" && (item.jobDiv === "SEA" || item.jobDiv === "AIR")) {
    const mode = item.jobDiv === "SEA" ? "sea" : "air";
    const bound = item.bound === "EXP" ? "exp" : "imp";
    return { blKind: "master", variantKey: `${mode}-${bound}` };
  }
  return null;
}

/**
 * B/L Copy 모달.
 *
 * - 소스 B/L 타입의 레지스트리에서 패널 목록을 가져와 체크박스 리스트 표시.
 * - house-bl-grid 패널은 항상 제외(자식 BL 참조 그리드).
 * - 기본 전체 체크. 확정 → use-bl-copy.executeCopy 호출.
 */
export function CopyBlModal({ item, isOpen, onClose }: Props) {
  const t = useTranslations("shell.quickSearch");

  const panelList = useMemo(() => resolvePanelList(item), [item]);
  const meta = useMemo(() => resolveBlMeta(item), [item]);

  const supportedPanelKeys = useMemo(
    () => (panelList ? panelList.map((p) => p.key) : []),
    [panelList],
  );

  const blKind = meta?.blKind;
  // variantKey는 Truck/Non-BL처럼 "" 빈 문자열이 유효한 값이므로 undefined 체크만 수행
  const variantKey = meta?.variantKey;
  const panelFieldsMap = useMemo(
    () => (blKind != null && variantKey != null ? getPanelFieldsMap(blKind, variantKey) : {}),
    [blKind, variantKey],
  );

  const [selectedKeys, setSelectedKeys] = useState<Set<string>>(
    () => new Set(supportedPanelKeys),
  );
  const [isCopying, setIsCopying] = useState(false);

  const { executeCopy } = useBlCopy();

  const handleToggle = useCallback((key: string) => {
    setSelectedKeys((prev) => {
      const next = new Set(prev);
      if (next.has(key)) {
        next.delete(key);
      } else {
        next.add(key);
      }
      return next;
    });
  }, []);

  const handleSelectAll = useCallback(() => {
    setSelectedKeys(new Set(supportedPanelKeys));
  }, [supportedPanelKeys]);

  const handleConfirm = useCallback(async () => {
    if (!meta || selectedKeys.size === 0) return;

    // 패널 목록 순서 유지, panelFieldsMap에 있는 키(필드 1개 이상)만 전달
    const orderedKeys = supportedPanelKeys.filter((k) => selectedKeys.has(k));
    const validKeys = orderedKeys.filter((k) => k in panelFieldsMap && panelFieldsMap[k].length > 0);

    setIsCopying(true);
    try {
      await executeCopy(item, validKeys);
      onClose();
    } finally {
      setIsCopying(false);
    }
  }, [meta, selectedKeys, supportedPanelKeys, panelFieldsMap, executeCopy, item, onClose]);

  const handleCancel = useCallback(() => {
    if (isCopying) return;
    onClose();
  }, [isCopying, onClose]);

  return (
    <ModalShell isOpen={isOpen} title={t("copyTitle")} size="default">
      <div className="modal__body">
        {panelList == null || meta == null ? (
          <p className="copy-bl-modal__unsupported">{t("copyUnsupported")}</p>
        ) : (
          <>
            <p className="copy-bl-modal__desc">{t("copyDesc")}</p>
            <div className="copy-bl-modal__actions-top">
              <button
                type="button"
                className="copy-bl-modal__select-all"
                onClick={handleSelectAll}
              >
                {t("copySelectAll")}
              </button>
            </div>
            <ul className="copy-bl-modal__panel-list">
              {panelList.map((panel) => {
                const hasFields = panel.key in panelFieldsMap && panelFieldsMap[panel.key].length > 0;
                return (
                  <li key={panel.key} className="copy-bl-modal__panel-item">
                    <label className="copy-bl-modal__panel-label">
                      <input
                        type="checkbox"
                        className="copy-bl-modal__panel-checkbox"
                        checked={selectedKeys.has(panel.key)}
                        disabled={!hasFields}
                        onChange={() => handleToggle(panel.key)}
                      />
                      <span>{panel.label}</span>
                    </label>
                  </li>
                );
              })}
            </ul>
          </>
        )}
      </div>
      <div className="modal__footer">
        <Button
          variant="normal"
          type="button"
          onClick={handleCancel}
          disabled={isCopying}
        >
          {t("copyCancel")}
        </Button>
        {panelList != null && meta != null && (
          <Button
            variant="transaction"
            type="button"
            onClick={handleConfirm}
            loading={isCopying}
            disabled={selectedKeys.size === 0}
          >
            {t("copyConfirm")}
          </Button>
        )}
      </div>
    </ModalShell>
  );
}
