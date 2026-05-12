"use client";

import { TextBox, NumberBox, ComboBox, CodeBox } from "@/components/shared/inputs";
import { useEnumOptions } from "@/application/enums/use-enum";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";

// House BL form 상태와 완전 격리 — RHF useFormContext/useController/register/Controller 불사용.
// 마이그레이션 시점에 schema/매핑 일괄 정리 예정.
export function SeaCargoPanel() {
  const { options: weightUnitOptions } = useEnumOptions("WeightUnit");

  const CARGO_ITEMS: FieldItemDef[] = [
    {
      key: "main-item",
      render: () => (
        <div className="li">
          <span className="li__label">Main Item</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="Main Item" />
          </div>
        </div>
      ),
    },
    {
      key: "hs-code",
      render: () => (
        <div className="li">
          <span className="li__label">HS Code</span>
          <div className="li__input">
            <TextBox variant="panel" placeholder="HS Code" />
          </div>
        </div>
      ),
    },
    {
      key: "package",
      render: () => (
        <div className="li">
          <span className="li__label">Package</span>
          <div className="li__input li__input--tight">
            <NumberBox variant="panel" decimalPlaces={0} placeholder="0" />
            <CodeBox kind="code-only" variant="panel" codeProps={{}} onLookup={() => {}} />
          </div>
        </div>
      ),
    },
    {
      key: "gross-wt",
      render: () => (
        <div className="li">
          <span className="li__label">Gross W/T</span>
          <div className="li__input li__input--tight">
            <NumberBox variant="panel" decimalPlaces={3} />
            <ComboBox variant="panel" options={weightUnitOptions} value="" onChange={() => {}} />
          </div>
        </div>
      ),
    },
    {
      key: "cbm",
      render: () => (
        <div className="li">
          <span className="li__label">CBM</span>
          <div className="li__input">
            <NumberBox variant="panel" decimalPlaces={3} />
          </div>
        </div>
      ),
    },
    {
      key: "rton",
      render: () => (
        <div className="li">
          <span className="li__label">R/Ton</span>
          <div className="li__input">
            <NumberBox variant="panel" decimalPlaces={3} />
          </div>
        </div>
      ),
    },
  ];

  return (
    <div className="panel panel--col-flex">
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Cargo</span>
      </div>
      <div className="panel__body panel__body--scroll">
        <FieldItemGrid itemScope="sea-cargo-panel" items={CARGO_ITEMS} />
      </div>
    </div>
  );
}
