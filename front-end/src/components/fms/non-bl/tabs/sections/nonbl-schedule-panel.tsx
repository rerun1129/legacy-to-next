"use client";

import { useMemo } from "react";
import { useFormContext, Controller } from "react-hook-form";
import { TextBox, CodeBox, DateBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";

export function NonBLSchedulePanel() {
  const { register, control } = useFormContext<NonBlFormValues>();

  const linerVesselItems: FieldItemDef[] = useMemo(() => [
    {
      key: "liner",
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label="Liner"
          codeProps={{ ...register("linerCode"), placeholder: "Code" }}
          nameProps={{ ...register("linerName"), placeholder: "Liner" }}
          onLookup={() => {}}
        />
      ),
    },
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
    {
      key: "voy",
      render: () => (
        <div className="li">
          <span className="li__label">Voy</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Voyage No" {...register("voyNo")} />
          </div>
        </div>
      ),
    },
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
  ], [register, control]);

  const portItems: FieldItemDef[] = useMemo(() => [
    {
      key: "pol",
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label="POL"
          required
          codeProps={{ ...register("polCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("polName"), placeholder: "Port Name" }}
          onLookup={() => {}}
        />
      ),
    },
    {
      key: "pod",
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label="POD"
          required
          codeProps={{ ...register("podCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("podName"), placeholder: "Port Name" }}
          onLookup={() => {}}
        />
      ),
    },
    {
      key: "final-dest",
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label="Final Dest."
          codeProps={{ ...register("finalDestCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("finalDestName"), placeholder: "Port Name" }}
          onLookup={() => {}}
        />
      ),
    },
    {
      key: "final-eta",
      render: () => (
        <div className="li">
          <span className="li__label">Final ETA</span>
          <div className="li__input">
            <Controller
              control={control}
              name="finalEta"
              render={({ field }) => (
                <DateBox
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
  ], [register, control]);

  const fields: FieldWidgetDef[] = [
    {
      key:    "liner-vessel",
      label:  "Liner & Vessel",
      render: () => <FieldItemGrid itemScope="nonbl-schedule-panel.liner" items={linerVesselItems} />,
    },
    {
      key:    "ports",
      label:  "Ports",
      render: () => <FieldItemGrid itemScope="nonbl-schedule-panel.ports" items={portItems} />,
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Schedule</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope="nonbl-schedule-panel" fields={fields} />
      </div>
    </div>
  );
}
