"use client";

import { useState } from "react";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { SectionProps } from "./_shared";
import { sectionStyle } from "./_shared";

/** 표준 입력 컴포넌트는 autoComplete="off"가 기본 적용됩니다. */
export function CodeSection({ form, variant, required, readOnly, disabled }: SectionProps) {
  const { register } = form;
  const [lcnLabelKind, setLcnLabelKind] = useState("ETD");

  return (
    <>
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>CodeBox (kind=lcn)</div>
        <CodeBox
          kind="lcn"
          label="Port of Loading"
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          onLookup={() => alert("Lookup: POL")}
          codeProps={{ placeholder: "POL Code", ...register("code") }}
          nameProps={{ placeholder: "Port Name", ...register("codeName") }}
        />
      </div>

      <div style={sectionStyle}>
        <CodeBox
          kind="lcn"
          variant={variant}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          label="Schedule"
          labelOptions={[
            { value: "POL", label: "POL" },
            { value: "POD", label: "POD" },
            { value: "Final Dest", label: "Final Dest" },
          ]}
          labelValue={lcnLabelKind}
          onLabelChange={setLcnLabelKind}
          codeProps={{ placeholder: "UNLOC", ...register("code") }}
          nameProps={{ placeholder: "Port Name", ...register("codeName") }}
          onLookup={() => alert(`Lookup: ${lcnLabelKind}`)}
        />
      </div>

      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>CodeBox (kind=party-cn)</div>
        <div style={{ display: "flex", gap: 8 }}>
          <CodeBox
            kind="party-cn"
            required={required}
            readOnly={readOnly}
            disabled={disabled}
            onLookup={() => alert("Lookup: Consignee")}
            codeProps={{ placeholder: "Code", ...register("partyCn") }}
            nameProps={{ placeholder: "Name", ...register("partyName") }}
          />
        </div>
      </div>
    </>
  );
}
