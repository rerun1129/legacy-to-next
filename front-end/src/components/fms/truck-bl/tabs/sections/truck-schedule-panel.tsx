"use client";

import { useFormContext, Controller } from "react-hook-form";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import { TextBox, CodeBox, DateBox } from "@/components/shared/inputs";
import type { TruckBlFormValues } from "@/components/fms/truck-bl/truck-bl-schema";

function TruckScheduleVessel() {
  const { register } = useFormContext<TruckBlFormValues>();
  const ITEMS: FieldItemDef[] = [
    {
      key: "vessel",
      render: () => (
        <div className="li">
          <span className="li__label">Vessel</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Vessel Name" {...register("vesselName")} />
          </div>
        </div>
      ),
    },
  ];
  return <FieldItemGrid itemScope="truck-schedule-panel.vessel" items={ITEMS} shouldShowRowControls={false} />;
}

function TruckScheduleDates() {
  const { control } = useFormContext<TruckBlFormValues>();
  const ITEMS: FieldItemDef[] = [
    {
      key: "etd",
      render: () => (
        <div className="li">
          <span className="li__label is-required">ETD</span>
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
          <span className="li__label is-required">ETA</span>
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
  return <FieldItemGrid itemScope="truck-schedule-panel.dates" items={ITEMS} shouldShowRowControls={false} />;
}

function TruckSchedulePol() {
  const { register } = useFormContext<TruckBlFormValues>();
  const ITEMS: FieldItemDef[] = [
    {
      key: "pol",
      render: () => (
        <CodeBox
          kind="lcn"
          label="POL"
          required
          codeProps={{ ...register("polCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("polName"), placeholder: "Location" }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
        />
      ),
    },
  ];
  return <FieldItemGrid itemScope="truck-schedule-panel.pol" items={ITEMS} cols={1} shouldShowRowControls={false} />;
}

function TruckSchedulePod() {
  const { register } = useFormContext<TruckBlFormValues>();
  const ITEMS: FieldItemDef[] = [
    {
      key: "pod",
      render: () => (
        <CodeBox
          kind="lcn"
          label="POD"
          required
          codeProps={{ ...register("podCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("podName"), placeholder: "Location" }}
          onLookup={() => {/* TODO(lookup): Phase C에서 구현 */}}
        />
      ),
    },
  ];
  return <FieldItemGrid itemScope="truck-schedule-panel.pod" items={ITEMS} cols={1} shouldShowRowControls={false} />;
}

export function TruckSchedulePanel() {
  const fields: FieldWidgetDef[] = [
    { key: "vessel", label: "Vessel", render: () => <TruckScheduleVessel /> },
    { key: "dates",  label: "Dates",  render: () => <TruckScheduleDates /> },
    { key: "pol",    label: "POL",    render: () => <TruckSchedulePol /> },
    { key: "pod",    label: "POD",    render: () => <TruckSchedulePod /> },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Schedule</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="truck-schedule-panel" fields={fields} />
      </div>
    </div>
  );
}
