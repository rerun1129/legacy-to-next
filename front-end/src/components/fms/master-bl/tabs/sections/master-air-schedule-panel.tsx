"use client";

import { useFormContext, useFieldArray, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { Plus, Minus } from "lucide-react";
import { GridList } from "@/components/shared/grid-list";
import { CodeBox, DateBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import type { MasterBlFormValues } from "../../master-bl-schema";
import { buildAirScheduleLegCols, type LegRow } from "@/components/fms/_shared/air-schedule-legs-cols";
import { Button } from "@/components/shared/button";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

interface Props { variant?: AnyVariantConfig }

export function MasterAirSchedulePanel({ variant }: Props) {
  const { register, control, setValue } = useFormContext<MasterBlFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: "scheduleLegs" });
  const tp = useTranslations("fms.masterBl.entry.panels");
  const tf = useTranslations("fms.masterBl.entry.fields");

  const airline     = useCodeAutocomplete(CODE_SOURCES.carrierAir);
  const departure   = useCodeAutocomplete(CODE_SOURCES.portAir);
  const destination = useCodeAutocomplete(CODE_SOURCES.portAir);

  if (!variant) return null;
  const panelScope = `master-air-schedule-panel.${variant.key}`;

  // Master schema: airDetail.airlineCode (airlineName 미존재 — name 없이 code only)
  const airlineItems: FieldItemDef[] = [
    {
      key: "airline",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label={tf("airline")}
          required
          codeProps={{ ...register("airDetail.airlineCode"), placeholder: "IATA" }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={airline.onSearch}
          suggestions={airline.suggestions}
          suggestionsLoading={airline.suggestionsLoading}
          onSelect={(it) => { setValue("airDetail.airlineCode", it.code); }}
        />
      ),
    },
    {
      key: "departure",
      render: () => (
        // Master schema: polCode (polName 미존재 — name 없이 code only)
        <CodeBox
          kind="lcn"
          variant="panel"
          label={tf("departure")}
          required
          codeProps={{ ...register("polCode"), placeholder: "UNLOC" }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={departure.onSearch}
          suggestions={departure.suggestions}
          suggestionsLoading={departure.suggestionsLoading}
          onSelect={(it) => { setValue("polCode", it.code); }}
        />
      ),
    },
  ];

  const widgetFields: FieldWidgetDef[] = [
    {
      key:   "airline",
      label: tf("airline"),
      render: () => (
        <FieldItemGrid
          itemScope={`${panelScope}.airline`}
          items={airlineItems}
          cols={2}
        />
      ),
    },
    {
      key:   "legs",
      label: tf("scheduleLegs"),
      render: () => {
        function handleAdd() {
          append({ toCode: "", byCarrier: "", flightNo: "", onBoardDt: "", onBoardTm: "", arrivalDt: "", arrivalTm: "" });
        }
        function handleRemove() {
          if (fields.length > 0) remove(fields.length - 1);
        }
        return (
          <div>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 4 }}>
              <span className="panel__rowcount">{fields.length}</span>
              <div className="panel__actions" style={{ display: "flex", gap: 4 }}>
                <Button variant="success" size="sm" iconOnly onClick={handleAdd}><Plus size={12} /></Button>
                <Button variant="danger" size="sm" iconOnly onClick={handleRemove} disabled={fields.length === 0}><Minus size={12} /></Button>
              </div>
            </div>
            <div style={{ overflow: "auto" }}>
              <GridList
                columns={buildAirScheduleLegCols(register, control, "scheduleLegs")}
                data={fields as unknown as LegRow[]}
                rowKey={(r) => String(r.id)}
              />
            </div>
          </div>
        );
      },
    },
    {
      key:   "destination",
      label: tf("destination"),
      render: () => (
        <FieldItemGrid
          itemScope={`${panelScope}.destination`}
          items={[{
            key: "destination",
            render: () => (
              // Master schema: podCode (podName 미존재 — name 없이 code only)
              <CodeBox
                kind="lcn"
                variant="panel"
                label={tf("destination")}
                required
                codeProps={{ ...register("podCode"), placeholder: "UNLOC" }}
                onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
                onSearch={destination.onSearch}
                suggestions={destination.suggestions}
                suggestionsLoading={destination.suggestionsLoading}
                onSelect={(it) => { setValue("podCode", it.code); }}
              />
            ),
          }]}
          cols={2}
        />
      ),
    },
    {
      key:   "dates",
      label: tf("onBoardArrival"),
      render: () => (
        <FieldItemGrid
          itemScope={`${panelScope}.dates`}
          items={[
            {
              key: "onboard",
              render: () => (
                <div className="li">
                  <span className="li__label is-required">{tf("onBoard")}</span>
                  <div className="li__input">
                    <Controller
                      control={control}
                      name="etd"
                      render={({ field }) => (
                        <DateBox
                          variant="panel"
                          required
                          ref={field.ref}
                          name={field.name}
                          value={field.value as string}
                          onChange={field.onChange}
                          onBlur={field.onBlur}
                        />
                      )}
                    />
                  </div>
                </div>
              ),
            },
            {
              key: "arrival",
              render: () => (
                <div className="li">
                  <span className="li__label is-required">{tf("arrival")}</span>
                  <div className="li__input">
                    <Controller
                      control={control}
                      name="eta"
                      render={({ field }) => (
                        <DateBox
                          variant="panel"
                          required
                          ref={field.ref}
                          name={field.name}
                          value={field.value as string}
                          onChange={field.onChange}
                          onBlur={field.onBlur}
                        />
                      )}
                    />
                  </div>
                </div>
              ),
            },
          ]}
          cols={2}
        />
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{tp("schedule")}</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={widgetFields} />
      </div>
    </div>
  );
}
