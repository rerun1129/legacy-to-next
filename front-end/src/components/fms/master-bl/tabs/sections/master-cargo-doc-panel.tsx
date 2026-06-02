"use client";

import { useFormContext, Controller, type UseFormReturn } from "react-hook-form";
import { useTranslations } from "next-intl";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { TextBox }    from "@/components/shared/inputs/text-box";
import { NumberBox }  from "@/components/shared/inputs/number-box";
import { CodeBox }    from "@/components/shared/inputs/code-box";
import { ComboBox }   from "@/components/shared/inputs/combo-box";
import { useEnumOptions } from "@/application/enums/use-enum";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

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

// ── HS Code LCN 필드 (bare: 외부 li__label 없이 label prop만) ─────────────────
function HsCodeLcnField() {
  const { register, setValue } = useFormContext<MasterBlFormValues>();
  const tf = useTranslations("fms.masterBl.entry.fields");
  const hsCodeAc = useCodeAutocomplete(CODE_SOURCES.hsCode);
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label={tf("hsCode")}
      codeProps={{ ...register("hsCode"), placeholder: "Code" }}
      nameProps={{ ...register("hsCodeName"), placeholder: "HS Code Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
      onSearch={hsCodeAc.onSearch}
      suggestions={hsCodeAc.suggestions}
      suggestionsLoading={hsCodeAc.suggestionsLoading}
      onSelect={(it) => { setValue("hsCode", it.code); setValue("hsCodeName", it.name); }}
    />
  );
}

// ── SEA Document LCN 필드 ──────────────────────────────────────────────────
// schema에 name 필드 없음 → nameProps는 placeholder만 (LinerLcnField와 동일 패턴)
function SettlePartnerLcnField() {
  const { register, setValue } = useFormContext<MasterBlFormValues>();
  const tf = useTranslations("fms.masterBl.entry.fields");
  const settlePartner = useCodeAutocomplete(CODE_SOURCES.partner);
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label={tf("settlePartner")}
      codeProps={{ ...register("settlePartnerCode"), placeholder: "Code" }}
      nameProps={{ placeholder: "Partner Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
      onSearch={settlePartner.onSearch}
      suggestions={settlePartner.suggestions}
      suggestionsLoading={settlePartner.suggestionsLoading}
      onSelect={(it) => { setValue("settlePartnerCode", it.code); }}
    />
  );
}

function OperatorLcnField() {
  const { register, setValue } = useFormContext<MasterBlFormValues>();
  const tf = useTranslations("fms.masterBl.entry.fields");
  const operatorAc = useCodeAutocomplete(CODE_SOURCES.user);
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label={tf("operator")}
      required
      codeProps={{ ...register("operatorCode"), placeholder: "Code" }}
      nameProps={{ placeholder: "Operator Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
      onSearch={operatorAc.onSearch}
      suggestions={operatorAc.suggestions}
      suggestionsLoading={operatorAc.suggestionsLoading}
      onSelect={(it) => { setValue("operatorCode", it.code); }}
    />
  );
}

function TeamLcnField() {
  const { register, setValue } = useFormContext<MasterBlFormValues>();
  const tf = useTranslations("fms.masterBl.entry.fields");
  const teamAc = useCodeAutocomplete(CODE_SOURCES.team);
  return (
    <CodeBox
      kind="lcn"
      variant="panel"
      label={tf("team")}
      required
      codeProps={{ ...register("teamCode"), placeholder: "Code" }}
      nameProps={{ ...register("teamName"), placeholder: "Team Name" }}
      onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
      onSearch={teamAc.onSearch}
      suggestions={teamAc.suggestions}
      suggestionsLoading={teamAc.suggestionsLoading}
      onSelect={(it) => { setValue("teamCode", it.code); setValue("teamName", it.name); }}
    />
  );
}

// ── G/W 필드 ───────────────────────────────────────────────────────────────
function GWField() {
  const { register, control } = useFormContext<MasterBlFormValues>();
  const tf = useTranslations("fms.masterBl.entry.fields");
  const { options: weightUnitOptions } = useEnumOptions("WeightUnit");
  return (
    <div className="li">
      <span className="li__label">{tf("grossWt")}</span>
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
  const { register, setValue } = useFormContext<MasterBlFormValues>();
  const tf = useTranslations("fms.masterBl.entry.fields");
  const pkgUnit = useCodeAutocomplete(CODE_SOURCES.packageUnit);
  return (
    <div className="li">
      <span className="li__label">{tf("package")}</span>
      <div className="li__input li__input--tight">
        <NumberBox variant="panel" decimalPlaces={0} placeholder="0" {...register("pkgQty")} />
        {/* pkgUnit: §6.14 정책 — 자유 텍스트(비표준 단위 가능) */}
        <div style={{ flex: "0 0 60px" }}>
          <CodeBox
            kind="code-only"
            variant="panel"
            codeProps={{ ...register("pkgUnit") }}
            onLookup={() => {}}
            onSearch={pkgUnit.onSearch}
            suggestions={pkgUnit.suggestions}
            suggestionsLoading={pkgUnit.suggestionsLoading}
            onSelect={(it) => { setValue("pkgUnit", it.code); }}
          />
        </div>
      </div>
    </div>
  );
}

export function MasterCargoDocPanel({ variant }: Props) {
  const tp = useTranslations("fms.masterBl.entry.panels");
  const tf = useTranslations("fms.masterBl.entry.fields");

  if (!variant) return null;
  const isSea      = variant.mode === "SEA";
  const panelScope = `master-cargo-doc.${variant.key}`;

  const cargoBase: FieldItemDef[] = [
    { key: "main-item", render: () => <LiText label={tf("mainItem")} name="mainItemName" /> },
    { key: "package",   render: () => <PackageField /> },
    { key: "gw",        render: () => <GWField /> },
    { key: "cbm",       render: () => (
      <div className="li">
        <span className="li__label">{tf("cbm")}</span>
        <div className="li__input"><NumberBox variant="panel" name="cbm" decimalPlaces={3} /></div>
      </div>
    )},
  ];

  const cargoExtras: FieldItemDef[] = isSea
    ? [{ key: "r-ton", render: () => (
        <div className="li">
          <span className="li__label">{tf("rton")}</span>
          {/* §6.64 — schema는 seaDetail.rton path. 마이그레이션 잔존 'rTon'은 미연결 path였음 */}
          <div className="li__input"><NumberBox variant="panel" name="seaDetail.rton" decimalPlaces={3} /></div>
        </div>
      )}]
    : [
        { key: "vol-wt",     render: () => (
          <div className="li">
            <span className="li__label">{tf("volumeWt")}</span>
            <div className="li__input"><NumberBox variant="panel" name="volWeight" decimalPlaces={3} /></div>
          </div>
        )},
        { key: "charge-wt",  render: () => (
          <div className="li">
            <span className="li__label">{tf("chargeWt")}</span>
            <div className="li__input"><NumberBox variant="panel" name="chargeWeight" decimalPlaces={3} /></div>
          </div>
        )},
        { key: "rate-class", render: () => <LiText label={tf("rateClass")} name="rateClass" /> },
      ];

  const cargoItems = [...cargoBase, ...cargoExtras];

  const seaDoc: FieldItemDef[] = [
    { key: "settle",   fullWidth: true, render: () => <SettlePartnerLcnField /> },
    { key: "operator", fullWidth: true, render: () => <OperatorLcnField /> },
    { key: "team",     fullWidth: true, render: () => <TeamLcnField /> },
  ];

  const airDocBase: FieldItemDef[] = [
    { key: "co-load-type", render: () => <LiText label={tf("coLoadType")} name="coLoadType" /> },
    { key: "flight-type",  render: () => <LiText label={tf("flightType")} name="flightType" /> },
  ];
  const airDocSec: FieldItemDef[] = variant.direction === "EXP"
    ? [{ key: "security", render: () => <LiText label={tf("securityStatus")} name="securityStatus" /> }]
    : [];
  const airDocTail: FieldItemDef[] = [
    { key: "settle",   render: () => <LiText label={tf("settlePartner")} name="settlePartnerCode" /> },
    { key: "operator", render: () => <LiText label={tf("operator")}      name="operatorCode" /> },
    { key: "team",     render: () => <LiText label={tf("team")}          name="teamCode" /> },
  ];

  const docItems = isSea ? seaDoc : [...airDocBase, ...airDocSec, ...airDocTail];

  const fields: FieldWidgetDef[] = [
    {
      key: "cargo", label: tf("cargo"),
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />{tf("cargo")}</div>
          <FieldItemGrid itemScope={`${panelScope}.cargo`} items={cargoItems} />
          <FieldItemGrid itemScope={`${panelScope}.hs`} items={[{ key: "hs-code", fullWidth: true, render: () => <HsCodeLcnField /> }]} cols={2} />
        </>
      ),
    },
    {
      key: "document", label: tf("document"),
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />{tf("document")}</div>
          <FieldItemGrid itemScope={`${panelScope}.document`} items={docItems} cols={2} />
        </>
      ),
    },
  ];

  return (
    <div className="panel master-cargo-doc-panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("cargoDocument")}</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
