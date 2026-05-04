import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { RadioBox } from "../radio-box";

const OPTIONS = [
  { value: "A", label: "Option A" },
  { value: "B", label: "Option B" },
  { value: "C", label: "Option C" },
];

describe("RadioBox", () => {
  it("required=true → 각 option input에 is-required 클래스 없음 + required 속성, label span에 is-required 클래스", () => {
    render(<RadioBox name="mode" options={OPTIONS} required label="테스트" />);
    const inputs = screen.getAllByRole("radio");
    for (const input of inputs) {
      expect(input).not.toHaveClass("is-required");
      expect(input).toBeRequired();
    }
    const labelSpan = document.querySelector(".rdo__label");
    expect(labelSpan).toHaveClass("is-required");
  });

  it("readOnly=true → 모든 input이 disabled", () => {
    render(<RadioBox name="mode" options={OPTIONS} readOnly />);
    const inputs = screen.getAllByRole("radio");
    for (const input of inputs) {
      expect(input).toBeDisabled();
    }
  });

  it("disabled=true → 모든 input이 disabled", () => {
    render(<RadioBox name="mode" options={OPTIONS} disabled />);
    const inputs = screen.getAllByRole("radio");
    for (const input of inputs) {
      expect(input).toBeDisabled();
    }
  });

  it("옵션 클릭 시 onChange 호출", () => {
    const handleChange = vi.fn();
    render(<RadioBox name="mode" options={OPTIONS} onChange={handleChange} />);
    const inputs = screen.getAllByRole("radio");
    fireEvent.click(inputs[1]);
    expect(handleChange).toHaveBeenCalledTimes(1);
  });

  it("모든 input이 동일한 name 속성 보유", () => {
    render(<RadioBox name="mode" options={OPTIONS} />);
    const inputs = screen.getAllByRole("radio");
    for (const input of inputs) {
      expect(input).toHaveAttribute("name", "mode");
    }
  });
});
