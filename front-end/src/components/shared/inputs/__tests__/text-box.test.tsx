import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { useForm } from "react-hook-form";
import { TextBox } from "../text-box";

function RhfWrapper() {
  const { register, getValues } = useForm<{ name: string }>();
  return (
    <>
      <TextBox {...register("name")} data-testid="input" />
      <button
        type="button"
        data-testid="get-btn"
        onClick={() => {
          (document.getElementById("rhf-output") as HTMLElement).textContent =
            getValues("name");
        }}
      >
        get
      </button>
      <span id="rhf-output" />
    </>
  );
}

describe("TextBox", () => {
  it("RHF 통합: register spread 후 입력 시 getValues에 반영", () => {
    render(<RhfWrapper />);
    const input = screen.getByTestId("input");
    fireEvent.change(input, { target: { value: "hello" } });
    fireEvent.click(screen.getByTestId("get-btn"));
    expect(document.getElementById("rhf-output")?.textContent).toBe("hello");
  });

  it("required prop → boxShadow에 inset 3px 0 0 포함", () => {
    render(<TextBox required data-testid="input" />);
    const input = screen.getByTestId("input");
    expect(input.style.boxShadow).toContain("inset 3px 0 0");
  });

  it("readOnly prop → DOM readOnly attribute === true", () => {
    render(<TextBox readOnly data-testid="input" />);
    expect(screen.getByTestId("input")).toHaveAttribute("readonly");
  });

  it("variant=cell → className에 grid__cell-input 포함", () => {
    render(<TextBox variant="cell" data-testid="input" />);
    expect(screen.getByTestId("input")).toHaveClass("grid__cell-input");
  });
});
