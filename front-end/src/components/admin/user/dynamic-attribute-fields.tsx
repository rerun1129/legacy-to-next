"use client";

import { ComboBox } from "@/components/shared/inputs/combo-box";
import type { ModuleAttributeDto } from "@/domain/access/attribute";

interface Props {
  attributes: ModuleAttributeDto[];
  values: Record<string, string[]>;
  onChange: React.Dispatch<React.SetStateAction<Record<string, string[]>>>;
  disabled?: boolean;
}

export function DynamicAttributeFields({ attributes, values, onChange, disabled }: Props) {
  return (
    <>
      {attributes.map((attr) => (
        <div className="lcn" key={attr.attributeKey}>
          <span className="lcn__label">{attr.name}</span>
          <div style={{ display: "flex", gap: 12, alignItems: "center", flexWrap: "wrap" }}>
            {attr.valueType === "ENUM" && attr.allowMulti && attr.values.map((opt) => (
              <label key={opt.value} style={{ display: "flex", alignItems: "center", gap: 4 }}>
                <input
                  type="checkbox"
                  checked={(values[attr.attributeKey] ?? []).includes(opt.value)}
                  disabled={disabled}
                  onChange={(e) => {
                    const prev = values[attr.attributeKey] ?? [];
                    const next = e.target.checked
                      ? [...prev, opt.value]
                      : prev.filter((v) => v !== opt.value);
                    onChange((p) => ({ ...p, [attr.attributeKey]: next }));
                  }}
                />
                {opt.label}
              </label>
            ))}
            {attr.valueType === "ENUM" && !attr.allowMulti && (
              <ComboBox
                variant="panel"
                options={attr.values.map((v) => ({ value: v.value, label: v.label }))}
                value={(values[attr.attributeKey] ?? [])[0] ?? ""}
                onChange={(e) =>
                  onChange((p) => ({
                    ...p,
                    [attr.attributeKey]: e.target.value ? [e.target.value] : [],
                  }))
                }
                disabled={disabled}
              />
            )}
            {attr.valueType === "STRING" && (
              <input
                className="box-panel"
                value={(values[attr.attributeKey] ?? [])[0] ?? ""}
                disabled={disabled}
                onChange={(e) =>
                  onChange((p) => ({
                    ...p,
                    [attr.attributeKey]: e.target.value ? [e.target.value] : [],
                  }))
                }
              />
            )}
            {attr.valueType === "NUMBER" && (
              <input
                type="number"
                className="box-panel"
                value={(values[attr.attributeKey] ?? [])[0] ?? ""}
                disabled={disabled}
                onChange={(e) =>
                  onChange((p) => ({
                    ...p,
                    [attr.attributeKey]: e.target.value ? [e.target.value] : [],
                  }))
                }
              />
            )}
          </div>
        </div>
      ))}
    </>
  );
}
