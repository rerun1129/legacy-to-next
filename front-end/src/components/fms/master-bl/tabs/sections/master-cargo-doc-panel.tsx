"use client";

import { useFormContext, Controller, type UseFormReturn } from "react-hook-form";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { TextBox }    from "@/components/shared/inputs/text-box";
import { NumberBox }  from "@/components/shared/inputs/number-box";
import { CodeBox }    from "@/components/shared/inputs/code-box";
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

// ── SEA Document LCN 필드 ──────────────────────────────────────────────────
// schema에 name 필드 없음 → nameProps는 placeholder만 (LinerLcnField와 동일 패턴)
function SettlePartnerLcnField() {
  const { register } = useFormContext<MasterBlFormValues>();
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label="Settle Partner"
      codeProps={{ ...register("settlePartnerCode"), placeholder: "Code" }}
      nameProps={{ placeholder: "Partner Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
    />
  );
}

function OperatorLcnField() {
  const { register } = useFormContext<MasterBlFormValues>();
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label="Operator"
      required
      codeProps={{ ...register("operatorCode"), placeholder: "Code" }}
      nameProps={{ placeholder: "Operator Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
    />
  );
}

function TeamLcnField() {
  const { register } = useFormContext<MasterBlFormValues>();
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label="Team"
      required
      codeProps={{ ...register("teamCode"), placeholder: "Code" }}
      nameProps={{ placeholder: "Team Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
    />
  );
}

// ── G/W 필드 ───────────────────────────────────────────────────────────────
function GWField() {
  const { register, control } = useFormContext<MasterBlFormValues>();
  const { options: weightUnitOptions } = useEnumOptions("WeightUnit");
  return (
    <div className="li">
      <span className="li__label">G/W</span>
      <div className="li__input li__input--tight">
        <NumberBox variant="panel" decimalPlaces={3} {...register("grossWeightKg")} />
        <Controller
          name="weightUnit"
          control={control}
          render={({ field }) => (
            <ComboBox variant="panel" options={weightUnitOptions} value={field.value} onChange={field.onChange} style={{ flex: "0 0 60px" }} />
          )}
        />
      </div>
    </div>
  );
}

// ── Package Qty 필드 ────────────────────────────────────────────────────────
function PackageField() {
  const { register } = useFormContext<MasterBlFormValues>();
  return (
    <div className="li">
      <span className="li__label">Package</span>
      <div className="li__input li__input--tight">
        <NumberBox variant="panel" decimalPlaces={0} placeholder="0" {...register("pkgQty")} />
        {/* pkgUnit: §6.14 정책 — 자유 텍스트(비표준 단위 가능) */}
        <div style={{ flex: "0 0 60px" }}>
          <CodeBox kind="code-only" variant="panel" codeProps={{ ...register("pkgUnit") }} onLookup={() => {}} />
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
    { key: "main-item", render: () => <LiText label="Main Item" name="mainItemName" /> },
    { key: "hs-code",   render: () => <LiText label="HS Code"   name="hsCode" /> },
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
    { key: "settle",   render: () => <SettlePartnerLcnField /> },
    { key: "operator", render: () => <OperatorLcnField /> },
    { key: "team",     render: () => <TeamLcnField /> },
  ];

  const airDocBase: FieldItemDef[] = [
    { key: "co-load-type", render: () => <LiText label="Co-Load Type" name="coLoadType" /> },
    { key: "flight-type",  render: () => <LiText label="Flight Type"  name="flightType" /> },
  ];
  const airDocSec: FieldItemDef[] = variant.direction === "EXP"
    ? [{ key: "security", render: () => <LiText label="Security Status" name="securityStatus" /> }]
    : [];
  const airDocTail: FieldItemDef[] = [
    { key: "settle",   render: () => <LiText label="Settle Partner" name="settlePartnerCode" /> },
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
          <FieldItemGrid itemScope={`${panelScope}.document`} items={docItems} cols={1} />
        </>
      ),
    },
  ];

  return (
    <div className="panel master-cargo-doc-panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
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
