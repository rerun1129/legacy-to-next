"use client";

import { useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { useFormContext, useFieldArray } from "react-hook-form";
import { useTranslations } from "next-intl";
import { Minus } from "lucide-react";
import { getModeLabels } from "@/lib/bl-mode-labels";
import { formatDateDisplay } from "@/lib/date";
import { useEntryFocusStore, entryFocusKeys } from "@/lib/use-entry-focus-store";
import { useBLDraftStore } from "@/lib/use-bl-draft-store";
import { useTabs } from "@/lib/use-tabs";
import { getPageTitle } from "@/lib/bl-variants";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";

interface Props { variant?: AnyVariantConfig }

export function MasterHouseBLGrid({ variant }: Props) {
  const { control } = useFormContext<MasterBlFormValues>();
  // keyName을 "rhfKey"로 분리해 field.id(=스키마 숫자 id)와 RHF 내부 UUID를 구분
  const { fields, remove } = useFieldArray({
    control,
    name: "houseBls",
    keyName: "rhfKey",
  });
  const [selectedIdx, setSelectedIdx] = useState<number | null>(null);
  const focusedRowKeyRef = useRef<string | null>(null);
  const router      = useRouter();
  const queryClient = useQueryClient();
  const setFocus    = useEntryFocusStore((s) => s.setFocus);
  const clearDraft  = useBLDraftStore((s) => s.clearDraft);
  const addTab      = useTabs((s) => s.addTab);

  const tf = useTranslations("fms.masterBl.entry.fields");
  if (!variant) return null;
  const ml = getModeLabels(variant.mode);

  function captureFocusedRow() {
    const activeEl = document.activeElement as HTMLElement | null;
    const tr = activeEl?.closest("tr[data-row-key]") as HTMLElement | null;
    focusedRowKeyRef.current = tr?.dataset.rowKey ?? null;
  }

  function handleHblDoubleClick(houseBlId: number) {
    if (!variant) return;
    const variantKey = variant.key;
    const path = `/fms/house-bl/${variantKey}/entry`;
    // 프레시 조회: stale 캐시·draft 제거 후 House Entry 진입
    queryClient.invalidateQueries({ queryKey: ["house-bl", "detail", houseBlId] });
    clearDraft(`house:${variantKey}:${houseBlId}`);
    setFocus(entryFocusKeys.houseBl(variantKey), houseBlId);
    // hot-marker: Entry 진입 시 하이라이트 (§6.16)
    sessionStorage.setItem(`house-bl-entry:hot:${houseBlId}`, "1");
    addTab(getPageTitle(variant, "House", "Entry"), path);
    router.push(path);
  }

  function handleRemove() {
    if (fields.length === 0) return;
    const focused = focusedRowKeyRef.current;
    let targetIdx = -1;
    if (focused !== null) {
      targetIdx = fields.findIndex(f => (f as unknown as { rhfKey: string }).rhfKey === focused);
    }
    if (targetIdx === -1 && selectedIdx !== null) {
      targetIdx = selectedIdx;
    }
    if (targetIdx === -1) targetIdx = fields.length - 1;
    remove(targetIdx);
    setSelectedIdx(null);
    focusedRowKeyRef.current = null;
  }

  const isAir = variant.mode === "AIR";

  return (
    <div className="panel" style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{ml.hblList}</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <button
            type="button"
            className="btn btn--sm"
            onMouseDown={captureFocusedRow}
            onClick={handleRemove}
            disabled={fields.length === 0}
          >
            <Minus size={12} />
          </button>
          <button type="button" className="btn btn--sm">{tf("houseConsol")}</button>
        </div>
      </div>
      <div style={{ overflow: "auto", flex: 1 }}>
        <table className="grid--list">
          {isAir ? (
            <colgroup>
              <col style={{ width: "40px" }} />
              <col style={{ width: "120px" }} />
              <col style={{ width: "160px" }} />
              <col style={{ width: "160px" }} />
              <col style={{ width: "160px" }} />
              <col style={{ width: "80px" }} />
              <col style={{ width: "100px" }} />
              <col style={{ width: "100px" }} />
              <col style={{ width: "100px" }} />
            </colgroup>
          ) : (
            <colgroup>
              <col style={{ width: "40px" }} />
              <col style={{ width: "120px" }} />
              <col style={{ width: "160px" }} />
              <col style={{ width: "160px" }} />
              <col style={{ width: "160px" }} />
              <col style={{ width: "80px" }} />
              <col style={{ width: "80px" }} />
              <col style={{ width: "100px" }} />
              <col style={{ width: "100px" }} />
              <col style={{ width: "100px" }} />
              <col style={{ width: "100px" }} />
              <col style={{ width: "120px" }} />
              <col style={{ width: "100px" }} />
              <col style={{ width: "100px" }} />
              <col style={{ width: "100px" }} />
            </colgroup>
          )}
          {isAir ? (
            <thead>
              <tr>
                <th className="row-num">#</th>
                <th>{ml.hblNo}</th>
                <th>{tf("shipper")}</th>
                <th>{tf("consignee")}</th>
                <th>{tf("docPartner")}</th>
                <th className="is-num">{tf("package")}</th>
                <th className="is-num">{tf("grossWT")}</th>
                <th className="is-num">{tf("chargeWT")}</th>
                <th className="is-num">{tf("cbm")}</th>
              </tr>
            </thead>
          ) : (
            <thead>
              <tr>
                <th className="row-num">#</th>
                <th>{ml.hblNo}</th>
                <th>{tf("shipper")}</th>
                <th>{tf("consignee")}</th>
                <th>{tf("docPartner")}</th>
                <th className="is-num">{tf("package")}</th>
                <th>{tf("unit")}</th>
                <th className="is-num">{tf("grossWT")}</th>
                <th className="is-num">{tf("cbm")}</th>
                <th>{tf("etd")}</th>
                <th>{tf("eta")}</th>
                <th>{tf("vesselName")}</th>
                <th>{tf("voyageNo")}</th>
                <th>{tf("pol")}</th>
                <th>{tf("pod")}</th>
              </tr>
            </thead>
          )}
          <tbody>
            {fields.length === 0 && (
              <tr>
                <td colSpan={isAir ? 9 : 15} style={{ textAlign: "center", padding: 8, fontSize: 11, color: "var(--ink-3)" }}>
                  {tf("noRows")}
                </td>
              </tr>
            )}
            {fields.map((field, idx) => (
              <tr
                key={field.rhfKey}
                data-row-key={field.rhfKey}
                className={idx === selectedIdx ? "is-selected" : undefined}
                onClick={() => setSelectedIdx(idx)}
                style={{ cursor: "pointer" }}
              >
                <td className="row-num">{idx + 1}</td>
                <td className="cell-hbl">
                  <span
                    onDoubleClick={(e) => { e.stopPropagation(); handleHblDoubleClick(field.houseBlId); }}
                    style={{ cursor: "pointer" }}
                    title="더블클릭하여 House B/L Entry 열기"
                  >
                    {field.hblNo ?? ""}
                  </span>
                </td>
                <td>{field.shipperCode ?? ""}</td>
                <td>{field.consigneeCode ?? ""}</td>
                <td>{field.docPartnerCode ?? ""}</td>
                <td className="is-num cell-mono">{field.pkgQty ?? ""}</td>
                {isAir ? (
                  <>
                    <td className="is-num cell-mono">{field.grossWeightKg ?? ""}</td>
                    <td className="is-num cell-mono">{field.chargeWeightKg ?? ""}</td>
                    <td className="is-num cell-mono">{field.cbm ?? ""}</td>
                  </>
                ) : (
                  <>
                    <td>{field.pkgUnit ?? ""}</td>
                    <td className="is-num cell-mono">{field.grossWeightKg ?? ""}</td>
                    <td className="is-num cell-mono">{field.cbm ?? ""}</td>
                    <td>{formatDateDisplay(field.etd)}</td>
                    <td>{formatDateDisplay(field.eta)}</td>
                    <td>{field.vesselName ?? ""}</td>
                    <td>{field.voyageNo ?? ""}</td>
                    <td>{field.polCode ?? ""}</td>
                    <td>{field.podCode ?? ""}</td>
                  </>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
