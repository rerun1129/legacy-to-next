"use client";

import { useFormContext, type UseFormReturn } from "react-hook-form";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { TextBox }    from "@/components/shared/inputs/text-box";
import { NumberBox }  from "@/components/shared/inputs/number-box";
import { ComboBox }   from "@/components/shared/inputs/combo-box";
import { useEnumOptions } from "@/application/enums/use-enum";

interface Props {
  variant?: AnyVariantConfig;
  form?:    UseFormReturn<MasterBlFormValues>;
}

// ── 공통 단순 텍스트 필드 ──────────────────────────────────────────────────
function LiText({ label, name }: { label: string; name: string }) {
  const { register } = useFormContext();
  return (
    <div className="li">
      <span className="li__label">{label}</span>
      <div className="li__input">
        <TextBox variant="panel" {...register(name)} />
      </div>
    </div>
  );
}

// ── G/W 필드 (숫자 + 단위 미지원 — 단위는 Phase 8 toolbar 범위) ──────────
function GWField() {
  return (
    <div className="li">
      <span className="li__label">G/W</span>
      <div className="li__input">
        <NumberBox variant="panel" name="grossWeightKg" decimalPlaces={3} />
      </div>
    </div>
  );
}

// ── Package Qty 필드 ────────────────────────────────────────────────────────
function PackageField() {
  const { options: pkgOptions, placeholder: pkgPlaceholder } = useEnumOptions("PackType");
  return (
    <div className="li">
      <span className="li__label">Package</span>
      <div className="li__input" style={{ display: "flex", gap: 4 }}>
        <NumberBox variant="panel" name="pkgQty" style={{ flex: 1 }} />
        <div style={{ flexShrink: 0, width: 64 }}>
          <ComboBox
            variant="panel"
            options={pkgOptions}
            placeholder={pkgPlaceholder ?? "Unit"}
          />
        </div>
      </div>
    </div>
  );
}

export function MasterCargoDocPanel({ variant }: Props) {
  if (!variant) return null;
  const isSea      = variant.mode === "SEA";
  const panelScope = `master-cargo-doc.${variant.key}`;

  const cargoBase: FieldItemDef[] = [
    { key: "main-item", render: () => <LiText label="Main Item" name="cargoMainItem" /> },
    { key: "hs-code",   render: () => <LiText label="HS Code"   name="cargoHsCode" /> },
    { key: "package",   render: () => <PackageField /> },
    { key: "gw",        render: () => <GWField /> },
    { key: "cbm",       render: () => (
      <div className="li">
        <span className="li__label">CBM</span>
        <div className="li__input"><NumberBox variant="panel" name="cbm" decimalPlaces={3} /></div>
      </div>
    )},
  ];

  const cargoExtras: FieldItemDef[] = isSea
    ? [{ key: "r-ton", render: () => (
        <div className="li">
          <span className="li__label">R/Ton</span>
          <div className="li__input"><NumberBox variant="panel" name="rTon" decimalPlaces={3} /></div>
        </div>
      )}]
    : [
        { key: "vol-wt",     render: () => (
          <div className="li">
            <span className="li__label">Volume W/T</span>
            <div className="li__input"><NumberBox variant="panel" name="volWeight" decimalPlaces={3} /></div>
          </div>
        )},
        { key: "charge-wt",  render: () => (
          <div className="li">
            <span className="li__label">Charge W/T</span>
            <div className="li__input"><NumberBox variant="panel" name="chargeWeight" decimalPlaces={3} /></div>
          </div>
        )},
        { key: "rate-class", render: () => <LiText label="Rate Class" name="rateClass" /> },
      ];

  const cargoItems = [...cargoBase, ...cargoExtras];

  const seaDoc: FieldItemDef[] = [
    { key: "settle",   render: () => <LiText label="Settle Partner" name="settlePartner" /> },
    { key: "co-load",  render: () => <LiText label="Co-Load Agent"  name="coLoadAgent" /> },
    { key: "operator", render: () => <LiText label="Operator"       name="operatorCode" /> },
    { key: "team",     render: () => <LiText label="Team"           name="teamCode" /> },
  ];

  const airDocBase: FieldItemDef[] = [
    { key: "co-load-type",  render: () => <LiText label="Co-Load Type"  name="coLoadType" /> },
    { key: "co-load-agent", render: () => <LiText label="Co-Load Agent" name="coLoadAgent" /> },
    { key: "flight-type",   render: () => <LiText label="Flight Type"   name="flightType" /> },
  ];
  const airDocSec: FieldItemDef[] = variant.direction === "EXP"
    ? [{ key: "security", render: () => <LiText label="Security Status" name="securityStatus" /> }]
    : [];
  const airDocTail: FieldItemDef[] = [
    { key: "settle",   render: () => <LiText label="Settle Partner" name="settlePartner" /> },
    { key: "operator", render: () => <LiText label="Operator"       name="operatorCode" /> },
    { key: "team",     render: () => <LiText label="Team"           name="teamCode" /> },
  ];

  const docItems = isSea ? seaDoc : [...airDocBase, ...airDocSec, ...airDocTail];

  const fields: FieldWidgetDef[] = [
    {
      key: "cargo", label: "Cargo",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Cargo</div>
          <FieldItemGrid itemScope={`${panelScope}.cargo`} items={cargoItems} />
        </>
      ),
    },
    {
      key: "document", label: "Document",
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />Document</div>
          <FieldItemGrid itemScope={`${panelScope}.document`} items={docItems} />
        </>
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Cargo &amp; Document</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
