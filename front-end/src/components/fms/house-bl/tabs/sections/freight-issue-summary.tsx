"use client";

import { useMemo } from "react";
import { useTranslations } from "next-intl";
import { type SelectedFreightLine } from "./freight-issue-types";
import { resolveDocType } from "./freight-issue-utils";

interface FreightIssueSummaryProps {
  selectedLines: SelectedFreightLine[];
  ti: ReturnType<typeof useTranslations>;
  tf: ReturnType<typeof useTranslations>;
}

export function FreightIssueSummary({ selectedLines, ti, tf }: FreightIssueSummaryProps) {
  const totalSettle = useMemo(
    () => selectedLines.reduce((sum, l) => sum + (l.settleAmount ?? 0), 0),
    [selectedLines],
  );
  const totalLocal = useMemo(
    () => selectedLines.reduce((sum, l) => sum + (l.localAmount ?? 0), 0),
    [selectedLines],
  );
  const totalVat = useMemo(
    () => selectedLines.reduce((sum, l) => sum + (l.vat ?? 0), 0),
    [selectedLines],
  );
  const totalUsd = useMemo(
    () => selectedLines.reduce((sum, l) => sum + (l.usdAmount ?? 0), 0),
    [selectedLines],
  );

  // 단일 customer/docType 요약 (선행 검증 통과 전제, 방어적 표시용)
  const singleCustomer = selectedLines.length > 0 ? selectedLines[0].customerCode : "";
  const singleDocType  = selectedLines.length > 0 ? resolveDocType(selectedLines[0].financialDocType) : "";

  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr 1fr 1fr 1fr", gap: 8, marginBottom: 8 }}>
      <div className="field">
        <div className="field__label">{ti("customer")}</div>
        <div className="field__input">
          <input className="input" readOnly value={singleCustomer} />
        </div>
      </div>
      <div className="field">
        <div className="field__label">{tf("cols.financialDocType")}</div>
        <div className="field__input">
          <input className="input" readOnly value={singleDocType} />
        </div>
      </div>
      <div className="field">
        <div className="field__label">{ti("lineCols.settleAmount")}</div>
        <div className="field__input">
          <input className="input" readOnly value={totalSettle ? totalSettle.toFixed(2) : ""} style={{ textAlign: "right" }} />
        </div>
      </div>
      <div className="field">
        <div className="field__label">{ti("lineCols.localAmount")}</div>
        <div className="field__input">
          <input className="input" readOnly value={totalLocal ? totalLocal.toFixed(2) : ""} style={{ textAlign: "right" }} />
        </div>
      </div>
      <div className="field">
        <div className="field__label">{ti("lineCols.vat")}</div>
        <div className="field__input">
          <input className="input" readOnly value={totalVat ? totalVat.toFixed(2) : ""} style={{ textAlign: "right" }} />
        </div>
      </div>
      <div className="field">
        <div className="field__label">{ti("lineCols.usdAmount")}</div>
        <div className="field__input">
          <input className="input" readOnly value={totalUsd ? totalUsd.toFixed(2) : ""} style={{ textAlign: "right" }} />
        </div>
      </div>
    </div>
  );
}
