import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { useForm } from "react-hook-form";
import { fireEvent } from "@testing-library/react";
import { DropBox } from "../drop-box";
import type { DropBoxOption } from "../_types";

const OPTIONS: DropBoxOption[] = [
  { value: "A", label: "Apple" },
  { value: "B", label: "Banana" },
];

function RhfDropWrapper() {
  const { register, getValues } = useForm<{ field: string }>();
  return (
    <>
      <DropBox {...register("field")} options={OPTIONS} data-testid="sel" />
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

describe("DropBox", () => {
  it("placeholder 명시 시 첫 번째 option이 value=''", () => {
    render(<DropBox options={OPTIONS} placeholder="선택하세요" data-testid="sel" />);
    const sel = screen.getByTestId("sel") as HTMLSelectElement;
    expect(sel.options[0].value).toBe("");
  });

  it("placeholder 미명시 시 빈 option 없음", () => {
    render(<DropBox options={OPTIONS} data-testid="sel" />);
    const sel = screen.getByTestId("sel") as HTMLSelectElement;
    expect(sel.options[0].value).toBe("A");
  });

  it("options 배열이 option 엘리먼트로 렌더", () => {
    render(<DropBox options={OPTIONS} data-testid="sel" />);
    const sel = screen.getByTestId("sel") as HTMLSelectElement;
    expect(sel.options).toHaveLength(2);
    expect(sel.options[0].label).toBe("Apple");
    expect(sel.options[1].label).toBe("Banana");
  });

  it("RHF 통합: register spread 후 선택 시 getValues에 반영", () => {
    render(<RhfDropWrapper />);
    const sel = screen.getByTestId("sel");
    fireEvent.change(sel, { target: { value: "B" } });
    fireEvent.click(screen.getByTestId("get-btn"));
    expect(document.getElementById("rhf-output")?.textContent).toBe("B");
  });
});
