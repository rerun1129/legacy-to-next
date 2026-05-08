import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { useForm } from "react-hook-form";
import { ComboBox } from "../combo-box";
import type { ComboBoxOption } from "../_types";

const OPTIONS: ComboBoxOption[] = [
  { value: "A", label: "Apple" },
  { value: "B", label: "Banana" },
];

function RhfComboWrapper() {
  const { register, getValues } = useForm<{ field: string }>();
  return (
    <>
      <ComboBox {...register("field")} options={OPTIONS} data-testid="sel" />
      <button
        type="button"
        data-testid="get-btn"
        onClick={() => {
          (document.getElementById("rhf-output") as HTMLElement).textContent =
            getValues("field");
        }}
      >
        get
      </button>
      <span id="rhf-output" />
    </>
  );
}

describe("ComboBox", () => {
  it("placeholder 명시 시 input에 placeholder 속성이 설정됨", () => {
    render(<ComboBox options={OPTIONS} placeholder="선택하세요" data-testid="sel" />);
    const input = screen.getByTestId("sel") as HTMLInputElement;
    expect(input.placeholder).toBe("선택하세요");
  });

  it("placeholder 미명시 시 input에 placeholder 없음", () => {
    render(<ComboBox options={OPTIONS} data-testid="sel" />);
    const input = screen.getByTestId("sel") as HTMLInputElement;
    expect(input.placeholder).toBe("");
  });

  it("포커스 시 옵션 리스트가 렌더됨", () => {
    render(<ComboBox options={OPTIONS} data-testid="sel" />);
    const input = screen.getByTestId("sel");
    fireEvent.focus(input);
    expect(screen.queryByText("Apple")).toBeTruthy();
    expect(screen.queryByText("Banana")).toBeTruthy();
  });

  it("RHF 통합: 옵션 선택 시 getValues에 반영", () => {
    render(<RhfComboWrapper />);
    const input = screen.getByTestId("sel");
    fireEvent.focus(input);
    const bananaOpt = screen.getByText("Banana");
    fireEvent.mouseDown(bananaOpt);
    fireEvent.click(screen.getByTestId("get-btn"));
    expect(document.getElementById("rhf-output")?.textContent).toBe("B");
  });
});
