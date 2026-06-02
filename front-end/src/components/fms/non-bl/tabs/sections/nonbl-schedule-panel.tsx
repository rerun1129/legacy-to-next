"use client";

import { useMemo } from "react";
import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import { TextBox, CodeBox, DateBox } from "@/components/shared/inputs";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";
import type { NonBlFormValues } from "@/components/fms/non-bl/non-bl-schema";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

export function NonBLSchedulePanel() {
  // Rules of Hooks: ALL hooks unconditionally before any early-return
  const tf = useTranslations("fms.nonBl.entry.fields");
  const tp = useTranslations("fms.nonBl.entry.panels");

  const { register, control, setValue } = useFormContext<NonBlFormValues>();

  // Non-BL은 SEA/AIR 구분 없이 전체 소스 사용
  const liner    = useCodeAutocomplete(CODE_SOURCES.carrier);
  const pol      = useCodeAutocomplete(CODE_SOURCES.port);
  const pod      = useCodeAutocomplete(CODE_SOURCES.port);
  const finalDest = useCodeAutocomplete(CODE_SOURCES.port);

  const linerVesselItems: FieldItemDef[] = useMemo(() => [
    {
      key: "liner",
      fullWidth: true,
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label={tf("liner")}
          codeProps={{ ...register("linerCode"), placeholder: "Code" }}
          nameProps={{ ...register("linerName"), placeholder: tf("liner") }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={liner.onSearch}
          suggestions={liner.suggestions}
          suggestionsLoading={liner.suggestionsLoading}
          onSelect={(it) => { setValue("linerCode", it.code); setValue("linerName", it.name); }}
        />
      ),
    },
    {
      key: "vessel",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("vessel")}</span>
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
          <span className="li__label">{tf("voy")}</span>
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
          <span className="li__label is-required">{tf("etd")}</span>
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
          <span className="li__label is-required">{tf("eta")}</span>
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
  ], [register, control, liner, setValue, tf]);

  const portItems: FieldItemDef[] = useMemo(() => [
    {
      key: "pol",
      fullWidth: true,
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label={tf("pol")}
          required
          codeProps={{ ...register("polCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("polName"), placeholder: "Port Name" }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={pol.onSearch}
          suggestions={pol.suggestions}
          suggestionsLoading={pol.suggestionsLoading}
          onSelect={(it) => { setValue("polCode", it.code); setValue("polName", it.name); }}
        />
      ),
    },
    {
      key: "pod",
      fullWidth: true,
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label={tf("pod")}
          required
          codeProps={{ ...register("podCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("podName"), placeholder: "Port Name" }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={pod.onSearch}
          suggestions={pod.suggestions}
          suggestionsLoading={pod.suggestionsLoading}
          onSelect={(it) => { setValue("podCode", it.code); setValue("podName", it.name); }}
        />
      ),
    },
    {
      key: "final-dest",
      fullWidth: true,
      render: () => (
        <CodeBox
          variant="panel"
          kind="lcn"
          label={tf("finalDest")}
          codeProps={{ ...register("finalDestCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("finalDestName"), placeholder: "Port Name" }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={finalDest.onSearch}
          suggestions={finalDest.suggestions}
          suggestionsLoading={finalDest.suggestionsLoading}
          onSelect={(it) => { setValue("finalDestCode", it.code); setValue("finalDestName", it.name); }}
        />
      ),
    },
    {
      key: "final-eta",
      render: () => (
        <div className="li">
          <span className="li__label">{tf("finalEta")}</span>
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
  ], [register, control, pol, pod, finalDest, setValue, tf]);

  const fields: FieldWidgetDef[] = [
    {
      key:    "liner-vessel",
      label:  tf("linerVessel"),
      render: () => <FieldItemGrid itemScope="nonbl-schedule-panel.liner" items={linerVesselItems} />,
    },
    {
      key:    "ports",
      label:  tf("ports"),
      render: () => <FieldItemGrid itemScope="nonbl-schedule-panel.ports" items={portItems} />,
    },
  ];

  return (
    <div className="panel panel--col-flex">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("schedule")}</span>
      </div>
      <div className="panel__body panel__body--scroll">
        <FieldWidgetList panelScope="nonbl-schedule-panel" fields={fields} />
      </div>
    </div>
  );
}
