import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { NumberBox } from "../number-box";

describe("NumberBox", () => {
  it("type 속성이 number", () => {
    render(<NumberBox data-testid="nb" />);
    expect(screen.getByTestId("nb")).toHaveAttribute("type", "number");
  });

  it("step 속성 기본값이 any", () => {
    render(<NumberBox data-testid="nb" />);
    expect(screen.getByTestId("nb")).toHaveAttribute("step", "any");
  });

  it("variant=cell → grid__cell-input 클래스 포함", () => {
    render(<NumberBox variant="cell" data-testid="nb" />);
    expect(screen.getByTestId("nb")).toHaveClass("grid__cell-input");
  });
});
