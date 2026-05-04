"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { TextBox } from "@/components/shared/inputs/text-box";
import { TextArea } from "@/components/shared/inputs/text-area";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { NumberBox } from "@/components/shared/inputs/number-box";
import { DropBox } from "@/components/shared/inputs/drop-box";
import { LinkBox } from "@/components/shared/inputs/link-box";
import { RadioBox } from "@/components/shared/inputs/radio-box";
import type { BoxVariant } from "@/components/shared/inputs";

type FormValues = {
  text: string;
  area: string;
  code: string;
  codeName: string;
  partyCn: string;
  partyName: string;
  amount: string;
  amountInt: string;
  amountDec2: string;
  amountDec3: string;
  unit: string;
  linkUrl: string;
  linkMenu: string;
  radioMode: string;
};

const UNIT_OPTIONS = [
  { value: "KG", label: "KG" },
  { value: "LB", label: "LB" },
  { value: "MT", label: "MT" },
];

const sectionStyle: React.CSSProperties = {
  borderTop: "1px solid #ddd",
  padding: "12px 16px",
};

const toggleStyle = (active: boolean): React.CSSProperties => ({
  padding: "4px 10px",
  fontSize: 11,
  border: "1px solid #ccc",
  borderRadius: 4,
  cursor: "pointer",
  background: active ? "#1d4ed8" : "#fff",
  color: active ? "#fff" : "#333",
});

export function InputsSection() {
  const [variant, setVariant] = useState<BoxVariant>("panel");
  const [required, setRequired] = useState(false);
  const [readOnly, setReadOnly] = useState(false);

  const { register, getValues } = useForm<FormValues>({
    defaultValues: {
      text: "sample text",
      area: "line 1\nline 2",
      code: "KRPUS",
      codeName: "Busan Port",
      partyCn: "CONSIG",
      partyName: "Test Consignee",
      amount: "1000",
      amountInt: "",
      amountDec2: "",
      amountDec3: "",
      unit: "KG",
      linkUrl: "External Docs",
      linkMenu: "User Management",
      radioMode: "A",
    },
  });

  return (
    <div style={{ fontFamily: "inherit", fontSize: 12, maxWidth: 800, margin: "0 auto", padding: 24 }}>
      <h1 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12 }}>Inputs Preview</h1>

      {/* 토글 컨트롤 */}
      <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
        <button style={toggleStyle(variant === "panel")} onClick={() => setVariant("panel")}>
          panel
        </button>
        <button style={toggleStyle(variant === "cell")} onClick={() => setVariant("cell")}>
          cell
        </button>
        <button style={toggleStyle(required)} onClick={() => setRequired((v) => !v)}>
          required
        </button>
        <button style={toggleStyle(readOnly)} onClick={() => setReadOnly((v) => !v)}>
          readOnly
        </button>
        <button
          style={{ ...toggleStyle(false), marginLeft: 16 }}
          onClick={() => alert(JSON.stringify(getValues(), null, 2))}
        >
          getValues
        </button>
      </div>

      {/* TextBox */}
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>TextBox</div>
        <TextBox
          variant={variant}
          required={required}
          readOnly={readOnly}
          placeholder="텍스트 입력"
          {...register("text")}
        />
      </div>

      {/* TextArea */}
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>TextArea (lineNumbers=false)</div>
        <TextArea
          variant={variant}
          required={required}
          readOnly={readOnly}
          placeholder="텍스트 에리어"
          rows={3}
          {...register("area")}
        />
        <div style={{ fontWeight: 600, margin: "10px 0 6px" }}>TextArea (lineNumbers=true)</div>
        <TextArea
          lineNumbers
          required={required}
          readOnly={readOnly}
          style={{ minHeight: 80 }}
          placeholder="줄 번호 포함 텍스트 에리어"
          {...register("area")}
        />
      </div>

      {/* CodeBox – lcn */}
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>CodeBox (kind=lcn)</div>
        <CodeBox
          kind="lcn"
          label="Port of Loading"
          required={required}
          readOnly={readOnly}
          onLookup={() => alert("Lookup: POL")}
          codeProps={{ placeholder: "POL Code", ...register("code") }}
          nameProps={{ placeholder: "Port Name", ...register("codeName") }}
        />
      </div>

      {/* CodeBox – party-cn */}
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>CodeBox (kind=party-cn)</div>
        <div style={{ display: "flex", gap: 8 }}>
          <CodeBox
            kind="party-cn"
            required={required}
            readOnly={readOnly}
            onLookup={() => alert("Lookup: Consignee")}
            codeProps={{ placeholder: "Code", ...register("partyCn") }}
            nameProps={{ placeholder: "Name", ...register("partyName") }}
          />
        </div>
      </div>

      {/* NumberBox */}
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>NumberBox</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          {/* 기본: step=1, 정수 전용 */}
          <div>
            <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>기본 (정수, step=1)</div>
            <NumberBox
              variant={variant}
              required={required}
              readOnly={readOnly}
              placeholder="0"
              style={{ width: 160 }}
              {...register("amountInt")}
            />
          </div>
          {/* 소수점 2자리: 금액/환율용 */}
          <div>
            <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>decimalPlaces=2 (금액·환율)</div>
            <NumberBox
              variant={variant}
              required={required}
              readOnly={readOnly}
              decimalPlaces={2}
              style={{ width: 160 }}
              {...register("amountDec2")}
            />
          </div>
          {/* 소수점 3자리: 중량용 */}
          <div>
            <div style={{ fontSize: 10, color: "#666", marginBottom: 2 }}>decimalPlaces=3 (중량·부피)</div>
            <NumberBox
              variant={variant}
              required={required}
              readOnly={readOnly}
              decimalPlaces={3}
              style={{ width: 160 }}
              {...register("amountDec3")}
            />
          </div>
        </div>
      </div>

      {/* DropBox */}
      <div style={sectionStyle}>
        <div style={{ fontWeight: 600, marginBottom: 6 }}>DropBox</div>
        <DropBox
          variant={variant}
          required={required}
          readOnly={readOnly}
          options={UNIT_OPTIONS}
          placeholder="단위 선택"
          {...register("unit")}
        />
      </div>

      {/* LinkBox – External URL */}
      <div style={sectionStyle}>
        <LinkBox
          label="LinkBox (External URL)"
          variant={variant}
          required={required}
          readOnly={readOnly}
          onLink={() => alert("Navigate to: https://docs.example.com")}
          inputProps={{ placeholder: "Display Name", ...register("linkUrl") }}
        />
      </div>

      {/* LinkBox – Menu Route */}
      <div style={sectionStyle}>
        <LinkBox
          label="LinkBox (Menu Route)"
          variant={variant}
          required={required}
          readOnly={readOnly}
          onLink={() => alert("Navigate to: /admin/user-management")}
          inputProps={{ placeholder: "Display Name", ...register("linkMenu") }}
        />
      </div>

      {/* RadioBox */}
      <div style={sectionStyle}>
        <RadioBox
          label="RadioBox (Mode)"
          variant={variant}
          required={required}
          readOnly={readOnly}
          options={[
            { value: "A", label: "Option A" },
            { value: "B", label: "Option B" },
            { value: "C", label: "Option C" },
          ]}
          {...register("radioMode")}
        />
      </div>
    </div>
  );
}
