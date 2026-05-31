"use client";

import { useFormContext, Controller } from "react-hook-form";
import { useTranslations } from "next-intl";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";
import { TextBox, CodeBox, DateBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";
import { IssueSection } from "./schedule-panel-helpers";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";

interface Props { variant?: AnyVariantConfig }

// ── Schedule Panel ──────────────────────────────────────────
export function SchedulePanel({ variant }: Props) {
  const tf = useTranslations("fms.houseBl.entry.fields");
  const tp = useTranslations("fms.houseBl.entry.panels");
  const { register, control, setValue } = useFormContext<HouseBlFormValues>();

  const pol      = useCodeAutocomplete(CODE_SOURCES.portSea);
  const pod      = useCodeAutocomplete(CODE_SOURCES.portSea);
  const delivery = useCodeAutocomplete(CODE_SOURCES.portSea);
  const liner    = useCodeAutocomplete(CODE_SOURCES.carrierSea);
  const { options: noOfBlOptions, placeholder: noOfBlPlaceholder } = useEnumOptions("NoOfBl");

  if (!variant) return null;
  const panelScope = `schedule-panel.${variant.key}`;

  const PORT_ITEMS: FieldItemDef[] = [
    {
      key: "pol",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label="POL"
          required
          codeProps={{ ...register("pol"), placeholder: "UNLOC" }}
          nameProps={{ ...register("seaDetail.polName"), placeholder: "Port Name" }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={pol.onSearch}
          suggestions={pol.suggestions}
          suggestionsLoading={pol.suggestionsLoading}
          onSelect={(it) => { setValue("pol", it.code); setValue("seaDetail.polName", it.name); }}
        />
      ),
    },
    {
      key: "pod",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label="POD"
          required
          codeProps={{ ...register("pod"), placeholder: "UNLOC" }}
          nameProps={{ ...register("seaDetail.podName"), placeholder: "Port Name" }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={pod.onSearch}
          suggestions={pod.suggestions}
          suggestionsLoading={pod.suggestionsLoading}
          onSelect={(it) => { setValue("pod", it.code); setValue("seaDetail.podName", it.name); }}
        />
      ),
    },
    {
      key: "delivery",
      render: () => (
        <CodeBox
          kind="lcn"
          variant="panel"
          label="Delivery"
          codeProps={{ ...register("seaDetail.deliveryCode"), placeholder: "UNLOC" }}
          nameProps={{ ...register("seaDetail.deliveryName"), placeholder: "Port Name" }}
          onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
          onSearch={delivery.onSearch}
          suggestions={delivery.suggestions}
          suggestionsLoading={delivery.suggestionsLoading}
          onSelect={(it) => { setValue("seaDetail.deliveryCode", it.code); setValue("seaDetail.deliveryName", it.name); }}
        />
      ),
    },
  ];

  const linerItem: FieldItemDef = {
    key: "liner",
    render: () => (
      <CodeBox
        kind="lcn"
        variant="panel"
        label="Liner"
        codeProps={{ ...register("seaDetail.linerCode"), placeholder: "UNLOC" }}
        nameProps={{ ...register("linerName") }}
        onLookup={() => {/* TODO(lookup): 모달 미구현. 별도 작업 후속. */}}
        onSearch={liner.onSearch}
        suggestions={liner.suggestions}
        suggestionsLoading={liner.suggestionsLoading}
        onSelect={(it) => { setValue("seaDetail.linerCode", it.code); setValue("linerName", it.name); }}
      />
    ),
  };

  const vesselItem: FieldItemDef = {
    key: "vessel",
    render: () => (
      <div className="li">
        <span className="li__label">{tf("vessel")}</span>
        <div className="li__input">
          <TextBox variant="panel" {...register("seaDetail.vesselName")} />
        </div>
      </div>
    ),
  };

  const voyageItem: FieldItemDef = {
    key: "voyage",
    render: () => (
      <div className="li">
        <span className="li__label">{tf("voyage")}</span>
        <div className="li__input">
          <TextBox variant="panel" {...register("seaDetail.voyageNo")} />
        </div>
      </div>
    ),
  };

  const etdItem: FieldItemDef = {
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
  };

  const etaItem: FieldItemDef = {
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
  };

  const onBoardItem: FieldItemDef = {
    key: "on-board",
    render: () => (
      <div className="li">
        <span className="li__label">{tf("onBoard")}</span>
        <div className="li__input">
          <Controller
            control={control}
            name="seaDetail.onboardDate"
            render={({ field }) => (
              <DateBox
                variant="panel"
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
  };

  const fields: FieldWidgetDef[] = [
    {
      key:   "liner",
      label: tf("linerVessel"),
      render: () => (
        <>
          <FieldItemGrid itemScope={`${panelScope}.liner.solo`}         items={[linerItem]}              cols={1} />
          <FieldItemGrid itemScope={`${panelScope}.liner.vessel-voyage`} items={[vesselItem, voyageItem]} cols={2} />
          <FieldItemGrid itemScope={`${panelScope}.liner.etd-eta`}      items={[etdItem, etaItem]}       cols={2} />
          <FieldItemGrid itemScope={`${panelScope}.liner.onboard`}      items={[onBoardItem]}            cols={2} />
        </>
      ),
    },
    {
      key:   "ports",
      label: tf("ports"),
      render: () => (
        <>
          <div className="subhead"><div className="subhead__bar" />{tf("ports")}</div>
          <FieldItemGrid itemScope={`${panelScope}.ports`} items={PORT_ITEMS} cols={1} />
        </>
      ),
    },
    ...(variant.issueFields.length > 0
      ? [{ key: "issue", label: tf("issueInformation"), render: () => <IssueSection issueFields={variant.issueFields} panelScope={panelScope} noOfBlOptions={noOfBlOptions} noOfBlPlaceholder={noOfBlPlaceholder} /> }]
      : []),
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tp("schedule")}</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
