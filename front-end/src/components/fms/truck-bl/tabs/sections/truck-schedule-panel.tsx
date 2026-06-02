"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import { TextBox, CodeBox, DateBox } from "@/components/shared/inputs";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

function TruckScheduleVessel({ vesselLabel }: { vesselLabel: string }) {
  const { register } = useFormContext<TruckBlFormValues>();
  const ITEMS: FieldItemDef[] = [
    {
      key: "vessel",
      render: () => (
        <div className="li">
          <span className="li__label">{vesselLabel}</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Vessel Name" {...register("vesselName")} />
          </div>
        </div>
      ),
    },
  ];
  return <FieldItemGrid itemScope="truck-schedule-panel.vessel" items={ITEMS} />;
}

function TruckScheduleDates({ etdLabel, etaLabel }: { etdLabel: string; etaLabel: string }) {
  const { control } = useFormContext<TruckBlFormValues>();
  const ITEMS: FieldItemDef[] = [
    {
      key: "etd",
      render: () => (
        <div className="li">
          <span className="li__label is-required">{etdLabel}</span>
          <div className="li__input">
            <Controller
              control={control}
              name="etd"
              render={({ field }) => (
                <DateBox
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
      key: "eta",
      render: () => (
        <div className="li">
          <span className="li__label is-required">{etaLabel}</span>
          <div className="li__input">
            <Controller
              control={control}
              name="eta"
              render={({ field }) => (
                <DateBox
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
  ];
  return <FieldItemGrid itemScope="truck-schedule-panel.dates" items={ITEMS} />;
}

function TruckSchedulePol({ polLabel }: { polLabel: string }) {
  const { register, setValue } = useFormContext<TruckBlFormValues>();
  const pol = useCodeAutocomplete(CODE_SOURCES.port);
  const ITEMS: FieldItemDef[] = [
    {
      key: "pol",
      fullWidth: true,
      render: () => (
        <CodeBox
          kind="lcn"
          label={polLabel}
          required
          codeProps={{ ...register("polCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("polName"), placeholder: "Location" }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          onSearch={pol.onSearch}
          suggestions={pol.suggestions}
          suggestionsLoading={pol.suggestionsLoading}
          onSelect={(it) => { setValue("polCode", it.code); setValue("polName", it.name); }}
        />
      ),
    },
  ];
  return <FieldItemGrid itemScope="truck-schedule-panel.pol" items={ITEMS} cols={2} />;
}

function TruckSchedulePod({ podLabel }: { podLabel: string }) {
  const { register, setValue } = useFormContext<TruckBlFormValues>();
  const pod = useCodeAutocomplete(CODE_SOURCES.port);
  const ITEMS: FieldItemDef[] = [
    {
      key: "pod",
      fullWidth: true,
      render: () => (
        <CodeBox
          kind="lcn"
          label={podLabel}
          required
          codeProps={{ ...register("podCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("podName"), placeholder: "Location" }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
          onSearch={pod.onSearch}
          suggestions={pod.suggestions}
          suggestionsLoading={pod.suggestionsLoading}
          onSelect={(it) => { setValue("podCode", it.code); setValue("podName", it.name); }}
        />
      ),
    },
  ];
  return <FieldItemGrid itemScope="truck-schedule-panel.pod" items={ITEMS} cols={2} />;
}

export function TruckSchedulePanel() {
  // Rules of Hooks: unconditionally at top
  const tf = useTranslations("fms.truckBl.entry.fields");
  const tp = useTranslations("fms.truckBl.entry.panels");

  const fields: FieldWidgetDef[] = [
    { key: "vessel", label: tf("vessel"), render: () => <TruckScheduleVessel vesselLabel={tf("vessel")} /> },
    { key: "dates",  label: tf("dates"),  render: () => <TruckScheduleDates etdLabel={tf("etd")} etaLabel={tf("eta")} /> },
    { key: "pol",    label: tf("pol"),    render: () => <TruckSchedulePol polLabel={tf("pol")} /> },
    { key: "pod",    label: tf("pod"),    render: () => <TruckSchedulePod podLabel={tf("pod")} /> },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">{tp("schedule")}</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="truck-schedule-panel" fields={fields} />
      </div>
    </div>
  );
}
