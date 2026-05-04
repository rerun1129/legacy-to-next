"use client";

import { useMemo } from "react";
import { useFormContext, Controller } from "react-hook-form";
import { Search } from "lucide-react";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";

export function NonBLSchedulePanel() {
  const { register, control } = useFormContext<NonBlFormValues>();

  const linerVesselItems: FieldItemDef[] = useMemo(() => [
    {
      key: "liner",
      render: () => (
        <div className="li">
          <span className="li__label">Liner</span>
          <div className="li__input" style={{ gap: 4 }}>
            <input placeholder="Code" style={{ width: 72, height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} {...register("linerCode")} />
            <input placeholder="Liner Name" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} {...register("linerName")} />
          </div>
        </div>
      ),
    },
    {
      key: "vessel",
      render: () => (
        <div className="li">
          <span className="li__label">Vessel</span>
          <div className="li__input">
            <input placeholder="Vessel Name" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} {...register("vesselName")} />
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
            <input placeholder="Voyage No" style={{ width: "100%", height: 22, padding: "0 8px", fontSize: 10 }} {...register("voyNo")} />
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
                <PanelDateInput
                  required
                  value={field.value as string}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  ref={field.ref}
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
                <PanelDateInput
                  required
                  value={field.value as string}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  ref={field.ref}
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
        <div className="lcn" style={{ marginBottom: 4 }}>
          <span className="lcn__label is-required">POL</span>
          <div className="lcn__code" style={{ position: "relative" }}>
            <input placeholder="UNLOC" style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} {...register("polCode")} />
            <Search size={12} className="lcn__icon" />
          </div>
          <input className="lcn__name" placeholder="Port Name" {...register("polName")} />
        </div>
      ),
    },
    {
      key: "pod",
      render: () => (
        <div className="lcn" style={{ marginBottom: 4 }}>
          <span className="lcn__label is-required">POD</span>
          <div className="lcn__code" style={{ position: "relative" }}>
            <input placeholder="UNLOC" style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} {...register("podCode")} />
            <Search size={12} className="lcn__icon" />
          </div>
          <input className="lcn__name" placeholder="Port Name" {...register("podName")} />
        </div>
      ),
    },
    {
      key: "final-dest",
      render: () => (
        <div className="lcn" style={{ marginBottom: 4 }}>
          <span className="lcn__label">Final Dest.</span>
          <div className="lcn__code" style={{ position: "relative" }}>
            <input placeholder="UNLOC" style={{ width: "100%", height: 22, padding: "0 24px 0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} {...register("finalDestCode")} />
            <Search size={12} className="lcn__icon" />
          </div>
          <input className="lcn__name" placeholder="Port Name" {...register("finalDestName")} />
        </div>
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
                <PanelDateInput
                  value={field.value as string}
                  onChange={field.onChange}
                  onBlur={field.onBlur}
                  ref={field.ref}
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
