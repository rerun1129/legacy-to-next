"use client";

import { useTranslations } from "next-intl";
import { useFormContext, useWatch, Controller, type Path } from "react-hook-form";
import { TextBox, NumberBox, CodeBox, DateBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

// ── 운임 당사자 항목 (readOnly + useWatch) ─────────────────
// nested path ("seaDetail.linerCode" 등)도 허용하므로 string으로 선언
interface CustomerItemProps {
  label:    string;
  codeName: string;
  nameName: string;
}

function CustomerItem({ label, codeName, nameName }: CustomerItemProps) {
  const { control } = useFormContext<HouseBlFormValues>();
  // nested path는 Path<HouseBlFormValues>로 캐스팅 — 런타임에 정상 동작, 타입 안전성은 호출 측에서 보장
  const code = useWatch({ control, name: codeName as Path<HouseBlFormValues> }) as string | undefined;
  const name = useWatch({ control, name: nameName as Path<HouseBlFormValues> }) as string | undefined;
  return (
    <div className="party-block__head">
      <span style={{ fontSize: 10, minWidth: 110, flexShrink: 0 }}>{label}</span>
      <div className="party-cn">
        <div className="party-cn__code">
          <TextBox variant="panel" readOnly value={code ?? ""} />
        </div>
        <TextBox variant="panel" readOnly value={name ?? ""} />
      </div>
    </div>
  );
}

// ── 환율 항목 ─────────────────────────────────────────────

// SELLING / BUYING: DateBox(100) + CodeBox currency(60) + NumberBox(140)
interface SellingBuyingExRateProps {
  label:    string;
  dateName: Path<HouseBlFormValues>;
  curName:  Path<HouseBlFormValues>;
  rateName: Path<HouseBlFormValues>;
}

function SellingBuyingExRate({ label, dateName, curName, rateName }: SellingBuyingExRateProps) {
  const { control, register, setValue } = useFormContext<HouseBlFormValues>();
  const currency = useCodeAutocomplete(CODE_SOURCES.currency);
  return (
    <div className="party-block__head">
      <span style={{ fontSize: 10, minWidth: 110, flexShrink: 0, color: "var(--ink-2)" }}>{label}</span>
      <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
        <div style={{ width: 100, flexShrink: 0 }}>
          <Controller
            name={dateName}
            control={control}
            render={({ field }) => (
              <DateBox
                variant="panel"
                name={field.name}
                value={(field.value as string) ?? ""}
                onChange={field.onChange}
                onBlur={field.onBlur}
              />
            )}
          />
        </div>
        {/* CodeBox 루트 .lcn은 display:grid라 60px wrapper를 무시하고 옆 칸을 침범 — display:block으로 해제 */}
        <div style={{ width: 60, flexShrink: 0 }}>
          <CodeBox
            kind="code-only"
            variant="panel"
            style={{ display: "block", width: "100%", padding: 0 }}
            codeProps={{ ...register(curName) }}
            onLookup={() => {}}
            onSearch={currency.onSearch}
            suggestions={currency.suggestions}
            suggestionsLoading={currency.suggestionsLoading}
            onSelect={(it) => { setValue(curName, it.code); }}
          />
        </div>
        <div style={{ width: 140, flexShrink: 0 }}>
          <NumberBox
            variant="panel"
            name={rateName}
            valueAsNumber={false}
            decimalPlaces={2}
            style={{ width: "100%" }}
          />
        </div>
      </div>
    </div>
  );
}

// USD: DateBox(100) + NumberBox(140)
interface UsdExRateProps {
  label:    string;
  dateName: Path<HouseBlFormValues>;
  rateName: Path<HouseBlFormValues>;
}

function UsdExRate({ label, dateName, rateName }: UsdExRateProps) {
  const { control } = useFormContext<HouseBlFormValues>();
  return (
    <div className="party-block__head">
      <span style={{ fontSize: 10, minWidth: 110, flexShrink: 0, color: "var(--ink-2)" }}>{label}</span>
      <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
        <div style={{ width: 100, flexShrink: 0 }}>
          <Controller
            name={dateName}
            control={control}
            render={({ field }) => (
              <DateBox
                variant="panel"
                name={field.name}
                value={(field.value as string) ?? ""}
                onChange={field.onChange}
                onBlur={field.onBlur}
              />
            )}
          />
        </div>
        <div style={{ width: 140, flexShrink: 0 }}>
          <NumberBox
            variant="panel"
            name={rateName}
            valueAsNumber={false}
            decimalPlaces={2}
            style={{ width: "100%" }}
          />
        </div>
      </div>
    </div>
  );
}

// ── Freight Information Panel ──────────────────────────────
interface FreightRatePanelProps {
  mode?: "SEA" | "AIR";
}

export function FreightRatePanel({ mode }: FreightRatePanelProps) {
  const tf = useTranslations("fms.houseBl.entry.freight");

  // liner 항목: SEA="Liner"+seaDetail.linerCode+linerName(root), AIR="Airline"+airDetail.airlineCode+airDetail.airlineName
  const linerLabel    = mode === "AIR" ? tf("customers.airline") : tf("customers.liner");
  const linerCodeName = mode === "AIR" ? "airDetail.airlineCode" : "seaDetail.linerCode";
  const linerNameName = mode === "AIR" ? "airDetail.airlineName" : "linerName";

  const customerItems: FieldItemDef[] = [
    {
      key: "actual-customer",
      label: tf("customers.actualCustomer"),
      render: () => (
        <CustomerItem
          label={tf("customers.actualCustomer")}
          codeName="actualCustomerCode"
          nameName="actualCustomerName"
        />
      ),
    },
    {
      key: "liner",
      label: linerLabel,
      render: () => (
        <CustomerItem
          label={linerLabel}
          codeName={linerCodeName}
          nameName={linerNameName}
        />
      ),
    },
    {
      key: "settle-partner",
      label: tf("customers.settlePartner"),
      render: () => (
        <CustomerItem
          label={tf("customers.settlePartner")}
          codeName="settlePartnerCode"
          nameName="settlePartnerName"
        />
      ),
    },
  ];

  const exrateItems: FieldItemDef[] = [
    {
      key: "selling-rate",
      label: tf("exRate.selling"),
      render: () => (
        <SellingBuyingExRate
          label={tf("exRate.selling")}
          dateName="sellRateDt"
          curName="sellRateCurrencyCode"
          rateName="sellRate"
        />
      ),
    },
    {
      key: "buying-rate",
      label: tf("exRate.buying"),
      render: () => (
        <SellingBuyingExRate
          label={tf("exRate.buying")}
          dateName="buyRateDt"
          curName="buyRateCurrencyCode"
          rateName="buyRate"
        />
      ),
    },
    {
      key: "usd",
      label: tf("exRate.usd"),
      render: () => (
        <UsdExRate
          label={tf("exRate.usd")}
          dateName="usdRateDt"
          rateName="usdRate"
        />
      ),
    },
  ];

  const fields: FieldWidgetDef[] = [
    { key: "customers",    label: tf("headers.customers"),  render: () => <FieldItemGrid itemScope="freight-rate-v2.customers"       items={customerItems} cols={3} /> },
    { key: "ex-rate-info", label: tf("headers.exRateInfo"), render: () => <FieldItemGrid itemScope="freight-rate-v2.ex-rate-info-v2" items={exrateItems}   cols={3} /> },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{tf("panels.freightInformation")}</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="freight-rate-v2" fields={fields} />
      </div>
    </div>
  );
}
